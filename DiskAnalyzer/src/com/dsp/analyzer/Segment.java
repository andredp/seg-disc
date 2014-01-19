package com.dsp.analyzer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.dsp.libs.StdStats;

/**
 * 
 * @author andredp
 * 
 */
public class Segment {

  private static final int NOT_INITIALIZED = -1;
  
  private int _index         = NOT_INITIALIZED;
  private int _startPosition = NOT_INITIALIZED;
  private int _endPosition   = NOT_INITIALIZED;
  
  private double _origSalience  = NOT_INITIALIZED;
  private double _origWorkload  = NOT_INITIALIZED;
  private double _fixedSalience = NOT_INITIALIZED;
  private double _fixedWorkload = NOT_INITIALIZED;
  
  private boolean _needsFixing = false;
  
  private List<Double> _readings = new ArrayList<Double>();


  /**
   * 
   */
  public Segment() { }

  // == Getters
  public int getIndex() {
    return _index;
  }
  
  public double getOriginalSalience() {
    return _origSalience;
  }
  
  public double getFixedSalience() {
    return _fixedSalience;
  }

  public int getStartPosition() {
    return _startPosition;
  }

  public int getEndPosition() {
    return _endPosition;
  }

  public double getFixedWorkload() {
    return _fixedWorkload;
  }
  
  public double getOriginalWorkload() {
    return (_origWorkload == NOT_INITIALIZED ? _fixedWorkload : _origWorkload);
  }


  // == Setters  
  public void setFixedSalience(double salience) {
    _fixedSalience = salience;
  }
  
  public void resetWorkload() {
    if (_origWorkload == NOT_INITIALIZED) {
      _origWorkload = _fixedWorkload;
    }
    _fixedWorkload = 0.0;
  }
  
  /**
   * 
   * @param index
   * @param startPosition
   */
  void initialize(int index, int startPosition) {
    _startPosition = startPosition;
    _index = index;
  }
  
  /**
   * 
   * @param endPosition
   */
  void finalize(int endPosition) {
    _endPosition = endPosition;
    calculateSalience();
  }
  
  /**
   * 
   * @param load
   */
  public void addWorkload(double load) {
    _fixedWorkload += load;
  }
  
  /**
   * 
   * @return
   */
  public int numReadings() {
    return _readings.size();
  }
  
  /**
   * 
   */
  public int midPosition() {
    return (_startPosition + _endPosition) / 2;
  }
  
  /**
   * 
   * @return
   */
  public double correction() {
    return _origSalience - _fixedSalience;
  }
  
  /**
   * 
   * @return
   */
  public boolean isBroken() {
    return _origSalience != _fixedSalience;
  }
  
  /**
   * 
   */
  public void setAsFixed() {
    _needsFixing  = false;
  }
  
  /**
   * 
   */
  public void needsFixing() {
    _needsFixing = true;
  }
  
  /**
   * 
   */
  public boolean isFixed() {
    return !_needsFixing;
  }
  
  /**
   * 
   * @param read
   */
  public void addReading(double reading) {
    _readings.add(reading);
  }
  
  /**
   * 
   * @param seg
   */
  public void addAllReadings(Segment seg) {
    _readings.addAll(0, seg._readings);
    _startPosition = seg._startPosition;
  }
  
  /**
   * 
   */
  public void calculateSalience() {
    Double[] readings = _readings.toArray(new Double[0]);
    double mean = StdStats.mean(readings);
    double stddev = StdStats.stddev(readings);
    _fixedSalience = _origSalience = mean + stddev;
  }

  @Override
  public String toString() {
    DecimalFormat fdf = new DecimalFormat("0.00");
    DecimalFormat idf = new DecimalFormat("00");
    return "[" + (isBroken() ? "X" : " ") + "]" +
        "[" + idf.format(_index + 1) + "]" +
        " Orig. Salience: "    + fdf.format(_origSalience) + 
        " | Fixed Salience: "  + fdf.format(_fixedSalience) +
        " | Difference: "      + fdf.format(_origSalience - _fixedSalience) +
        " | Orig. Workload: "  + fdf.format(getOriginalWorkload()) +
        " | Fixed Workload: "  + fdf.format(_fixedWorkload);
  }
  
}
