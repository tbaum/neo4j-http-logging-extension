/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server.extension.httplog;

import com.sun.jersey.api.client.Client;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.neo4j.server.configuration.EmbeddedServerConfigurator;
import org.neo4j.server.configuration.ThirdPartyJaxRsPackage;
import org.neo4j.test.ImpermanentGraphDatabase;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;

/**
 * @author tbaum
 * @since 19.05.2012
 */
public class TestLogLine {

    @Test
    public void expectLogLine() throws IOException {
        File logFile = new File("target/logs/http.log");
        logFile.delete();

        ImpermanentGraphDatabase db = new ImpermanentGraphDatabase();

        EmbeddedServerConfigurator configurator = new EmbeddedServerConfigurator(db);
        Configuration config = configurator.configuration();
        config.setProperty("http.log.dir", logFile.getParent());
        config.setProperty("http.log.cfg", "conf/neo4j-server-test-logback.xml");

        configurator.getThirdpartyJaxRsClasses().add(new ThirdPartyJaxRsPackage("org.neo4j.server.extension.httplog", "/authlog"));

        WrappingNeoServerBootstrapper testBootstrapper = new WrappingNeoServerBootstrapper(db, configurator);
        testBootstrapper.start();


        Client client = Client.create();

        client.resource("http://localhost:7474/db/data").get(String.class);
        testBootstrapper.stop();


        Scanner file = new Scanner(logFile);
        String line = file.nextLine();

        assertTrue(line.contains("/db/data"));
    }
}
