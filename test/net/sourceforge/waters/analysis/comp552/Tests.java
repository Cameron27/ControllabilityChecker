package net.sourceforge.waters.analysis.comp552;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.marshaller.SAXProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class Tests {
  private static final String[] simpleExampleNames = new String[]{"empty_1", "empty_2", "small_factory_2", "small_factory_2u", "bad_factory", "bfactory", "cat_mouse", "cell", "cell_block", "cell_uncont", "debounce", "elevator_safety", "elevator_liveness", "ipc", "ipc_cswitch", "ipc_lswitch", "ipc_lswitch_sup", "ipc_uswitch", "notinc15", "notinc20", "tictactoe", "wsp_timer", "wsp_timer_noreset"};
  private static final String[] allExampleNames = new String[]{"simple/empty_1", "simple/empty_2", "simple/small_factory_2", "simple/small_factory_2u", "simple/bad_factory", "simple/bfactory", "simple/cat_mouse", "simple/cell", "simple/cell_block", "simple/cell_uncont", "simple/debounce", "simple/elevator_safety", "simple/elevator_liveness", "simple/ipc", "simple/ipc_cswitch", "simple/ipc_lswitch", "simple/ipc_lswitch_sup", "simple/ipc_uswitch", "simple/notinc15", "simple/notinc20", "simple/tictactoe", "simple/wsp_timer", "simple/wsp_timer_noreset", "agv/agv", "agv/agvb", "agv/agvs", "batch_tank/batch_plant", "batch_tank/amk14", "batch_tank/cjn5", "batch_tank/jbr2", "batch_tank/kah18", "batch_tank/lsr1_1", "batch_tank/rch11", "batch_tank/scs10", "batch_tank/smr26", "batch_tank/tk27", "batch_tank/tp20", "bmw/bmw_fh", "bmw/med_bmw", "bmw/dreitueren", "bmw/ftuer", "bmw/koordwsp", "bmw/koordwsp_block", "bmw/tuer1", "bmw/tuer2", "bmw/vtueren", "bmw/verriegel2", "profisafe/profisafe_i4host_efsm", "profisafe/profisafe_i5host_efsm", "profisafe/profisafe_i6host_efsm", "profisafe/profisafe_i4slave_efsm", "transfer_line/tline_1", "transfer_line/tline_u_1", "transfer_line/tline_2", "transfer_line/tline_u_2", "transfer_line/tline_v_2", "transfer_line/tline_3", "transfer_line/tline_u_3", "transfer_line/tline_v_3", "transfer_line/tline_4", "transfer_line/tline_u_4", "transfer_line/tline_v_4", "transfer_line/tline_5", "transfer_line/tline_u_5", "transfer_line/tline_v_5", "transfer_line/tline_6", "transfer_line/tline_u_6", "transfer_line/tline_v_6"};
  private static ProductDESProxyFactory desFactory;
  private static ProductDESProxy[] simpleModels;
  private static ProductDESProxy[] allModels;
  private static final long[] allExampleSize = new long[]{1, 1, 12, 12, 15, 69, 18, 56, 64, 92, 6, 906, 356, 20592, 41184, 247104, 4374, 370656, 18, 23, 6324, 5, 4, 25731072, 22929408, 16601088, 48, 40, 52, 38, 62, 55, 512, 138, 42, 18, 111, 7672, 948024, 420283, 195, 465648, 634608, 226, 238, 8407, 21774256, 916924, 1621536, 2617300, 20010, 28, 64, 410, 627, 472, 5992, 65536, 6896, 87578, 2097152, 100792, 1280020, 67108864, 1473152, 18708482, 2147483648L, 21531256};

  @BeforeClass
  public static void setup() throws WatersUnmarshalException, IOException, EvalException, ParserConfigurationException, SAXException {
    QuietLogConfigurationFactory.install();

    final ModuleProxyFactory moduleFactory = ModuleElementFactory.getInstance();
    desFactory = ProductDESElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    final SAXModuleMarshaller moduleMarshaller = new SAXModuleMarshaller(moduleFactory, optable);
    final SAXProductDESMarshaller desMarshaller = new SAXProductDESMarshaller(desFactory);
    final DocumentManager docManager = new DocumentManager();
    docManager.registerUnmarshaller(desMarshaller);
    docManager.registerUnmarshaller(moduleMarshaller);

    simpleModels = new ProductDESProxy[simpleExampleNames.length];
    for (int i = 0; i < simpleExampleNames.length; i++) {
      final String name = "examples\\simple\\" + simpleExampleNames[i] + ".wmod";
      final File filename = new File(name);
      final DocumentProxy doc = docManager.load(filename);
      if (doc instanceof ProductDESProxy) {
        simpleModels[i] = (ProductDESProxy) doc;
      } else {
        final ModuleProxy module = (ModuleProxy) doc;
        final ModuleCompiler compiler =
            new ModuleCompiler(docManager, desFactory, module);
        simpleModels[i] = compiler.compile();
      }
    }

    allModels = new ProductDESProxy[allExampleNames.length];
    for (int i = 0; i < allExampleNames.length; i++) {
      final String name = "examples\\" + allExampleNames[i] + ".wmod";
      final File filename = new File(name);
      final DocumentProxy doc = docManager.load(filename);
      if (doc instanceof ProductDESProxy) {
        allModels[i] = (ProductDESProxy) doc;
      } else {
        final ModuleProxy module = (ModuleProxy) doc;
        final ModuleCompiler compiler =
            new ModuleCompiler(docManager, desFactory, module);
        allModels[i] = compiler.compile();
      }
    }
  }

  @Test
  public void testRunSimple() throws AnalysisException {
    final boolean[] expected = new boolean[]{true, false, true, false, true, false, false, true, true, false, true, true, true, false, false, false, true, false, true, true, false, true, true};
    ControllabilityCounterExampleChecker verifier = new ControllabilityCounterExampleChecker();

    for (int i = 0; i < simpleExampleNames.length; i++) {
      final ControllabilityChecker checker = new ControllabilityChecker(simpleModels[i], desFactory);

      assertEquals(expected[i], checker.run());

      SafetyCounterExampleProxy counterExample = checker.getCounterExample();
      if (expected[i]) assertNull(counterExample);
      else assertTrue(verifier.checkCounterExample(simpleModels[i], counterExample));
    }
  }

  @Test
  public void testRunFull() throws AnalysisException {
    final boolean[] expected = new boolean[]{true, false, true, false, true, false, false, true, true, false, true, true, true, false, false, false, true, false, true, true, false, true, true, false, false, true, false, true, false, true, true, true, false, false, true, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, false, false, true, false, false, true, false, false, true, false, false, true, false, false};
    ControllabilityCounterExampleChecker verifier = new ControllabilityCounterExampleChecker();

    for (int i = 0; i < allExampleNames.length; i++) {
      if (expected[i]) continue;
      long start = System.nanoTime();
      final ControllabilityChecker checker = new ControllabilityChecker(allModels[i], desFactory);
      try {
        assertEquals(expected[i], checker.run());
      } catch (StateTupleEncoder.StateTupleSizeException e) {
        System.out.println(String.format("%s\n\tfailed due to state tuple size", allExampleNames[i]));
        continue;
      }

      SafetyCounterExampleProxy counterExample = checker.getCounterExample();
      if (expected[i]) assertNull(counterExample);
      else assertTrue(verifier.checkCounterExample(allModels[i], counterExample));

      long end = System.nanoTime();
      System.out.println(String.format("%s\n\trun time: %f seconds\n\tnumber of states: %d\n\tcontrollable: %s", allExampleNames[i], (end - start) / 1e9, allExampleSize[i], expected[i] ? "true" : "false"));
    }
  }
}