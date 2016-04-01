package com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class SBCLPipe {

  private String os = "";

  public SBCLPipe() {
    if (System.getProperty("os.name").startsWith("Windows")) {
      os = "Windows";
    } else {
      // Mac and Linux use same terminal cmds
      //TODO commands for mac/linux are currently untested
      os = "Linux";
    }
  }

  private Socket client;
  private BufferedReader input;
  private PrintStream output;

  public String sendCommand(String command) {
    output.println(command);
    output.flush();
    String result = "";
    Boolean end = false;
    long start = System.currentTimeMillis();
    try {
      while (!input.ready()) {
      }
      while (end == false) {
        result = result.concat(input.readLine());
        end = result.contains("</result>");
      }
      if (System.currentTimeMillis() - start > 5000) {
        return "error|5|";
      }
      return result;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "error|2|";
  }

  public String connect(String address, int port) throws InterruptedException, IOException {
    if (client == null || client.isClosed()) {
      System.out.println("starting server");
      startServer();
      Thread.sleep(2000);
      System.out.println("connecting");
      return connectIOBuffers(address, port);
    } else {
      System.out.println("killing existing server");
      killServer();
      Thread.sleep(2000);
      System.out.println("starting new server");
      startServer();
      Thread.sleep(2000);
      System.out.println("connecting");
      return connectIOBuffers(address, port);
    }
  }

  private void startServer() throws IOException {
    if (os == "Windows") {
      Runtime.getRuntime().exec(
          "cmd /c start /b cmd.exe /K \"cd BTAnalyser && sbcl --dynamic-space-size 4096 --load \"start.lisp\"");
    } else if (os == "Mac/Linux") {
      Runtime.getRuntime().exec(new String[] {"bash", "-c",
          "cd BTAnalyser; sudo sbcl --dynamic-space-size 4096 --load \"start.lisp\""});
    }
  }

  private void killServer() throws IOException {
    if (os == "Windows") {
      Runtime.getRuntime().exec("taskkill /F /T /IM sbcl.exe");
    } else if (os == "Linux") {
      Runtime.getRuntime().exec(new String[] {"bash", "-c",
          "kill -9 $(ps | grep sbcl | sed s/grep sbcl'// | grep sbcl | sed 's/\\([0-9][0-9]*\\).*/\\1/')"});
    }
  }

  private String connectIOBuffers(String address, int port) {
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
