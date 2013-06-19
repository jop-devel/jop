package prelude.app;

import javax.realtime.precedence.DepWord;
import javax.realtime.precedence.Precedence;

import prelude.*;

public class Preludesampling {

  private Preludesampling() {} // hide constructor
  private static int swap18_p_id21_i[] = new int[3];
  private static int i_swap18_i;
  private static int id21_o_swap18_j[] = {5, 5, 5};
  private static int swap18_o_o;
  
  private static int i0_out;
  private static int i0_instance=0;
  private static void i0_fun()
  {
    i0_out = sampling.input_i();
    
    i_swap18_i=i0_out;
    i0_instance++;
  }
  
  private static int id21_i_rcell=0;
  
  private static int id21_o_swap18_j_wcell=1;
  
  private static int id21_out;
  private static int id21_instance=0;
  private static void id21_fun()
  {
    id21_out = sampling.id(swap18_p_id21_i[id21_i_rcell]);
    
    id21_i_rcell=(id21_i_rcell+1)%3;
    id21_o_swap18_j[id21_o_swap18_j_wcell]=id21_out;
    id21_o_swap18_j_wcell=(id21_o_swap18_j_wcell+1)%3;
    id21_instance++;
  }
  
  private static final ComPattern swap18_j_change =
    new ComPattern(new boolean[]{ false, false, true },
          new boolean[]{ false, false, true });
  
  private static int swap18_j_rcell=0;
  
  private static final ComPattern swap18_p_id21_i_write =
    new ComPattern(null, new boolean[]{ true, false, false });
  
  private static int swap18_p_id21_i_wcell=0;
  
  public static class swap_OutType {
    public int o;
    public int p;
  };
  
  private static swap_OutType swap18_out = new swap_OutType();
  private static int swap18_instance=0;
  private static void swap18_fun()
  {
    sampling.swap(i_swap18_i, id21_o_swap18_j[swap18_j_rcell], swap18_out);
    
    if (swap18_j_change.mustUpdate(swap18_instance))
      swap18_j_rcell=(swap18_j_rcell+1)%3;
    swap18_o_o=swap18_out.o;
    if (swap18_p_id21_i_write.mustUpdate(swap18_instance)) {
      swap18_p_id21_i[swap18_p_id21_i_wcell]=swap18_out.p;
      swap18_p_id21_i_wcell=(swap18_p_id21_i_wcell+1)%3;
    }
    swap18_instance++;
  }
  
  private static int o0_instance=0;
  private static void o0_fun()
  {
    sampling.output_o(swap18_o_o);
    
    o0_instance++;
  }
  
  private static final PreludeTask[] taskSet = {
    new PreludeTask("i0", 500, 0, 50, 350)
    { public void run() { i0_fun(); } },
    new PreludeTask("id21", 1500, 0, 150, 1500)
    { public void run() { id21_fun(); } },
    new PreludeTask("swap18", 500, 0, 100, 450)
    { public void run() { swap18_fun(); } },
    new PreludeTask("o0", 500, 0, 50, 500)
    { public void run() { o0_fun(); } }
  };
  
  private static final DepWord[] i0_swap18_pcpat = { new DepWord(0,0) };
  private static final DepWord[] id21_swap18_pcpat = { new DepWord(0,3) };
  private static final DepWord[] swap18_o0_pcpat = { new DepWord(0,0) };
  private static final DepWord[] swap18_id21_pcpat = { new DepWord(0,0) };
  
  private static final PreludePrecedence[] precSet = {
    new PreludePrecedence("i0", "swap18",
      new Precedence(null, i0_swap18_pcpat)),
    new PreludePrecedence("id21", "swap18",
      new Precedence(null, id21_swap18_pcpat)),
    new PreludePrecedence("swap18", "o0",
      new Precedence(null, swap18_o0_pcpat)),
    new PreludePrecedence("swap18", "id21",
      new Precedence(null, swap18_id21_pcpat))
  };
  
  public static void main(String [] args) {
    PreludeSafelet.start(taskSet, precSet);
  }
  
}
