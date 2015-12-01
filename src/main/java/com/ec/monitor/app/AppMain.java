package com.ec.monitor.app;
import com.ec.commons.command.ServerShutdownOption;
import com.ec.commons.server.jetty.DispatchHandler;
import com.ec.commons.server.jetty.SimpleHTTPServer;
import com.ec.monitor.properties.NsqwatcherProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by xxg on 2015/12/1.
 */
public class AppMain {


        private static ApplicationContext applicationContext;
        private static Logger log = LogManager.getLogger(AppMain.class);

        public static AppMain appMain = null;
        private SimpleHTTPServer httpServer;
        private boolean waitingForGracefulShutdown = false;
        private static ThreadPoolExecutor imThreadPool;

        public static void main(String[] args) {
            try {
                appMain = new AppMain();
                appMain.init();
                appMain.start();
            } catch (Throwable t) {
                log.fatal("Fatal Error in main", t);
                System.exit(8);
            }
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private void init() throws Exception {
            applicationContext = new ClassPathXmlApplicationContext("classpath*:/spring/applicationContext.xml");
            log.info("=========>spring容器初始化完成...");

            httpServer = applicationContext.getBean(SimpleHTTPServer.class);
            NsqwatcherProperties properties = applicationContext.getBean(NsqwatcherProperties.class);
            imThreadPool = new ThreadPoolExecutor(20,20, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue());
        }

        private void start() throws Exception {
            try {
                log.info(this + " start httpServer");
                DispatchHandler dispatchHandler = applicationContext.getBean(DispatchHandler.class);
                httpServer.startServer(dispatchHandler);

            } catch (Exception e) {
                log.fatal(this + " Can't start server", e);
                System.exit(8);
            }
            try {
                synchronized (this) {
                    this.wait();
                }
                log.debug("Server Stoped!");
                System.exit(0);
            } catch (Exception e) {
                log.error(this + " Interrupted", e);
                System.exit(8);
            }
        }

        public void shutdownServer(ServerShutdownOption shutdownOption) {
            log.info("shutdownServer(), option=" + shutdownOption);

            if (shutdownOption == ServerShutdownOption.FORCEIBLE) {
                stop();
            } else if (shutdownOption == ServerShutdownOption.GRACEFUL_WAIT) {
                waitForGracefulShutdown();
                stop();
            } else {// by default is ServerShutdownOption.GRACEFUL
                if (!waitingForGracefulShutdown) {
                    final AppMain appMain = this;
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {

                                appMain.waitForGracefulShutdown();
                                log.info("Notify Server to stop");
                                appMain.stop();

                            } catch (Exception e) {
                                log.error(e);
                            }
                        }
                    };
                    thread.setName("WaitForGracefulShutdownThread");
                    thread.start();
                }
            }
        }

        public void waitForGracefulShutdown() {
            log.info("waitingForGracefulShutdown");
            waitingForGracefulShutdown = true;


            if (httpServer != null) {
                try {
                    httpServer.stopServer();
                    log.info("stoped httpServer");
                } catch (Exception e) {
                    log.error("关闭Jetty server发生异常", e);
                }
            }
        }

        public synchronized void stop() {
            this.notify();
        }

        public static ApplicationContext getApplicationContext() {
            return applicationContext;
        }

        public static ThreadPoolExecutor getIMThreadPool() {
            return imThreadPool;
        }

    }




