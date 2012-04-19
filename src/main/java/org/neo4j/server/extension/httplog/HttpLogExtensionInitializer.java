/**
 * Copyright (c) 2002-2012 "Neo Technology,"
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

import ch.qos.logback.access.jetty.RequestLogImpl;
import org.apache.commons.configuration.Configuration;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.NeoServer;
import org.neo4j.server.NeoServerWithEmbeddedWebServer;
import org.neo4j.server.logging.Logger;
import org.neo4j.server.plugins.Injectable;
import org.neo4j.server.plugins.SPIPluginLifecycle;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

public class HttpLogExtensionInitializer implements SPIPluginLifecycle {
    private static final Logger LOG = new Logger(HttpLogExtensionInitializer.class);

    @Override
    public Collection<Injectable<?>> start(final GraphDatabaseService graphDatabaseService, final Configuration config) {
        throw new IllegalAccessError();
    }

    public void stop() {
    }

    @Override
    public Collection<Injectable<?>> start(final NeoServer neoServer) {
        LOG.info("START " + HttpLogExtensionInitializer.class.toString());

        final Server jetty = getJetty(neoServer);
        final Configuration configuration = neoServer.getConfiguration();

        final String logDir = configuration.getString("http.log.dir");
        if (logDir == null) {
            throw new RuntimeException("missing http.log.dir neo4j-server.properties");
        }
        final String logCfg = configuration.getString("http.log.cfg");
        if (logCfg == null) {
            throw new RuntimeException("missing http.log.cfg neo4j-server.properties");
        }

        File logDirectory = new File(logDir);
        File logbackConfigFile = new File(logCfg);

        LOG.info("log_dir " + logDirectory.getAbsolutePath());
        LOG.info("config " + logbackConfigFile.getAbsolutePath());

        final RequestLogImpl requestLog = new RequestLogImpl();
        requestLog.putProperty("http_log_dir", logDirectory.getAbsolutePath());
        requestLog.setFileName(logbackConfigFile.getAbsolutePath());

        final RequestLogHandler requestLogHandler = new RequestLogHandler();
        requestLogHandler.setRequestLog(requestLog);


        jetty.addHandler(requestLogHandler);

        return Arrays.<Injectable<?>>asList();
    }

    private Server getJetty(final NeoServer neoServer) {
        if (neoServer instanceof NeoServerWithEmbeddedWebServer) {
            final NeoServerWithEmbeddedWebServer server = (NeoServerWithEmbeddedWebServer) neoServer;
            return server.getWebServer().getJetty();
        } else {
            throw new IllegalArgumentException("expected NeoServerWithEmbeddedWebServer");
        }
    }
}
