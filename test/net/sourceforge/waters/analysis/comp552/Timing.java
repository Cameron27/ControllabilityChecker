package net.sourceforge.waters.analysis.comp552;

import org.junit.Test;

public class Timing {
  @Test
  public void testRunSimple() {
    long start = System.nanoTime();
    ControllabilityMain.main(new String[]{"examples/bmw/koordwsp.wmod"});
    long end = System.nanoTime();
    System.out.println((end - start) / 1e9 + "seconds");


    start = System.nanoTime();
    ControllabilityMain.main(new String[]{"examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod examples/transfer_line/tline_v_6.wmod "});
    end = System.nanoTime();
    System.out.println((end - start) / 1e9 + "seconds");
  }
}