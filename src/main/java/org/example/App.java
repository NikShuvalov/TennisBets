package org.example;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class App {

//    public static void main(String[] args) throws IOException {
//        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
//
//        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
//
//        server.createContext("/health", Handlers::health);
//        server.createContext("/api/hello", Handlers::hello);
//        server.createContext("/api/items", new Handlers.ItemsHandler());
//
//        // Handle each request on a pool thread
//        server.setExecutor(Executors.newFixedThreadPool(8));
//        server.start();
//
//        System.out.println("Server running on http://localhost:" + port);
//    }
}
