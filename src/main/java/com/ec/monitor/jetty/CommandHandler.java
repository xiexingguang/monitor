package com.ec.monitor.jetty;

import com.ec.commons.command.ServerShutdownOption;
import com.ec.commons.server.jetty.IHandler;
import com.ec.commons.server.jetty.JettyUrl;
import com.ec.monitor.app.AppMain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;


@Component
@JettyUrl(target="/")
public class CommandHandler implements IHandler {
    private static Logger log = LogManager.getLogger(CommandHandler.class);

    @Override
    public void doRequest(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException,
                    ServletException {
        Writer writer = response.getWriter();
        String command = request.getParameter("command");
        log.info("received command " + command);
        if ("Stop".equalsIgnoreCase(command)) {
            AppMain.appMain.shutdownServer(ServerShutdownOption.GRACEFUL);
            writer.write("server stoping");
        } else {
            writer.write("unknown command");
        }
        writer.flush();
    }


}
