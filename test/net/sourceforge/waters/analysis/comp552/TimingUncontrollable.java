package net.sourceforge.waters.analysis.comp552;

import org.junit.Test;

import java.io.OutputStream;
import java.io.PrintStream;

public class TimingUncontrollable {
  @Test
  public void testRunSimple() {
    PrintStream out = System.out;
    System.setOut(new PrintStream(new OutputStream() {
      @Override
      public void write(int b) {

      }
    }));
    
    long start = System.nanoTime();
    ControllabilityMain.main(new String[]{"examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod", "examples/transfer_line/tline_v_6.wmod"});
    long end = System.nanoTime();
    System.setOut(out);
    System.out.println((end - start) / 1e9 + "seconds");
  }
}