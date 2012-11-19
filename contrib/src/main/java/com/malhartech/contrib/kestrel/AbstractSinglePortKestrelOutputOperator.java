/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.malhartech.contrib.kestrel;

import com.malhartech.api.DefaultInputPort;

/**
 * Kestrel output adapter operator, which send data to Kestrel message bus.<p><br>
 *
 * <br>
 * Ports:<br>
 * <b>Input</b>: Can have one input port<br>
 * <b>Output</b>: no output port<br>
 * <br>
 * Properties:<br>
 * None<br>
 * <br>
 * Compile time checks:<br>
 * None<br>
 * <br>
 * Run time checks:<br>
 * None<br>
 * <br>
 * <b>Benchmarks</b>: Blast as many tuples as possible in inline mode<br>
 * Operator can process about 1 thousand unique (k,v immutable pairs) tuples/sec as Kestrel DAG. The performance is directly proportional to key,val pairs emitted<br>
 * <br>
 * @author Zhongjian Wang <zhongjian@malhar-inc.com>
 */
public abstract class AbstractSinglePortKestrelOutputOperator<T> extends AbstractKestrelOutputOperator
{

  /**
   * Users need to provide conversion of Tuple to message
   * @param tuple
   */
  public abstract void processTuple(T tuple);

  public final transient DefaultInputPort<T> inputPort = new DefaultInputPort<T>(this)
  {
    @Override
    public void process(T tuple)
    {
      processTuple(tuple); // This is an abstract call
    }
  };
}
