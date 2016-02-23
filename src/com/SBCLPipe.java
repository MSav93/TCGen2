package com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class SBCLPipe {

  private Socket client;
  private BufferedReader input;
  private PrintStream output;

  public String sendCommand(String command) {
    output.println(command);
    String result = "";
    Boolean end = false;
    long start = System.currentTimeMillis();
    try {
      while (!input.ready()) {
      }
      while (end == false) {
        result = result.concat(input.readLine());
        end = result.contains("</result>");
        output.flush();
      }
      if (System.currentTimeMillis() - start > 5000) {
        return "error|5|";
      }
      System.out.println(result);
      return result;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "error|2|";
  }

  public String connect(String address, int port) throws InterruptedException, IOException {
    if (client == null || client.isClosed()) {
      System.out.println("starting server");
      Runtime.getRuntime().exec("cmd /c start /b cmd.exe /K \"cd BTAnalyser && start.cmd\"");
      Thread.sleep(2000);
      return connect2(address, port);
    } else {
      System.out.println("killing existing server");
      Runtime.getRuntime().exec("taskkill /F /T /IM sbcl.exe");
      Thread.sleep(2000);
      System.out.println("starting new server");
      Runtime.getRuntime().exec("cmd /c start /b cmd.exe /K \"cd BTAnalyser && start.cmd\"");
      Thread.sleep(2000);
      System.out.println("connecting");
      return connect2(address, port);
    }
  }

  private String connect2(String address, int port) {
    try {
      client = new Socket(address, port);
      input = new BufferedReader(new InputStreamReader(client.getInputStream()));
      output = new PrintStream(client.getOutputStream());
      return "success";
    } catch (UnknownHostException e) {
      return "error|0|" + e.getMessage();
    } catch (IOException e) {
      return "error|1|" + e.getMessage();
    }
  }

  public void close() throws IOException {
    input.close();
    output.close();
    client.close();
  }
}
