package com.dsp.analyzer;

import java.util.ArrayList;
import java.util.List;

import com.dsp.analyzer.config.Configurations;
import com.dsp.libs.StdStats;
import com.esotericsoftware.minlog.Log;

public class SegmentedDisc {

  private static final double MEAN_THRESHOLD = 0.9;

  private static final double[] VALID_READ_RANGE = Configurations.getInstance().getVector2d("VALID_READ_RANGE");
  private static final int      ERROR_THRESHOLD  = Configurations.getInstance().getInt("SEG_ERROR_THRESHOLD");

  private List<Segment> _frontSegments = new ArrayList<Segment>();
  private List<Segment> _backSegments = new ArrayList<Segment>();

  private boolean _brokenDisc = false;
  private double _maxWorkload;

  /**
   * 
   * @param data
   */
  public SegmentedDisc(DiscRawData data, double workTolerance) {    
    parseReadings(data.getFront(), _frontSegments);
    parseReadings(data.getBack(), _backSegments);

    if (_frontSegments.size() != _backSegments.size()) {
      Log.warn("SegmentedDisk", "Back and Front side have a different number of segments.");
    }
    
    _maxWorkload = workTolerance * _frontSegments.size();
    
    calculateWorkload(_frontSegments);
    calculateWorkload(_backSegments);
  }

  /**
   * 
   */
  public boolean isBrokenDisc() {
    return _brokenDisc;
  }

  /**
   * This method accepts indexes out of range as if the array was circular
   * (ring)
   */
  private Segment getSegment(List<Segment> disc, int index) {
    int wrappedIndex = index % disc.size();
    if (wrappedIndex < 0) { // java modulus can be negative
      wrappedIndex += disc.size();
    }
    return disc.get(wrappedIndex);
  }

  /**
   * NOTA: Sempre que o disco se encontra em boas condicoes, o valor de
   * _origWorkload permanece inalterado (-1) dado que o primeiro ciclo nao se
   * repete. Isto nao causa qualquer entropia. (fixed)
   * 
   * @param disc
   */
  private void calculateWorkload(List<Segment> disc) {
    boolean needsRecalculation = true;
    while (needsRecalculation) {
      needsRecalculation = false;
      for (Segment s : disc)
        s.resetWorkload();

      for (int ix = 0; ix < disc.size(); ix++) {
        Segment actual = getSegment(disc, ix);
        Segment next = getSegment(disc, actual.getIndex() - 1);
        Segment pivot = next;

        if (next.getFixedSalience() < actual.getFixedSalience()) {
          actual.addWorkload(actual.getFixedSalience() - next.getFixedSalience());
        }

        while (next.getFixedSalience() < actual.getFixedSalience()) {
          if (next.getFixedSalience() > pivot.getFixedSalience()) {
            pivot = next;
          }
          actual.addWorkload(actual.getFixedSalience()
              - pivot.getFixedSalience());
          if (actual.getFixedWorkload() > _maxWorkload) {
            actual.needsFixing();
            _brokenDisc = true;
            needsRecalculation = true;
          }
          next = getSegment(disc, next.getIndex() - 1);
        }

        if (!actual.isFixed()) {
          actual.setFixedSalience(pivot.getFixedSalience());
          actual.setAsFixed();
        }
      }
    }
  }

  /**
   * Analisa as leituras (em bruto) dadas pelo PLC e cria um "disco segmentado"
   * com o numero de segmentos, etc...
   * 
   * @param readings
   * @param storage
   */
  private void parseReadings(double[] readings, List<Segment> disc) {
    int consecutiveErrors = 0; // readings outside of a segment / misreading
    Segment segment = null; // segment being parsed

    for (int ix = 0; ix < readings.length; ix++) {
      if (readings[ix] >= VALID_READ_RANGE[0]
          && readings[ix] <= VALID_READ_RANGE[1]) {
        // Creating a segment if it needs
        if (segment == null) {
          segment = new Segment();
          segment.initialize(disc.size(), ix);
        }
        // Valid reading registering
        consecutiveErrors = 0;
        segment.addReading(readings[ix]);
      } else {
        consecutiveErrors++;
        if (consecutiveErrors == ERROR_THRESHOLD && segment != null) {
          segment.finalize(ix);
          disc.add(segment);
          segment = null;
        }
      }
    }

    // Calculating the mean of readings per segment to see if the last and the
    // first
    // segment need to concatenate (in case it started reading in the middle of
    // a segment)
    double[] segmentReadings = new double[disc.size() - 2]; // the first and
                                                            // last do not count
    for (int ix = 0; ix < segmentReadings.length; ix++) {
      segmentReadings[ix] = disc.get(ix + 1).numReadings();
    }
    double meanReadings = StdStats.mean(segmentReadings);

    Segment first = disc.get(0);
    Segment last = disc.get(disc.size() - 1);
    if (first.numReadings() < MEAN_THRESHOLD * meanReadings
        || last.numReadings() < MEAN_THRESHOLD * meanReadings) {
      first.addAllReadings(last);
      disc.remove(last);
    }
  }

  @Override
  public String toString() {
    String text = "=== SEGMENTED DISC ===";
    text += "\n\t[FRONT]\n";
    for (Segment s : _frontSegments)
      text += s + "\n";
    text += "\n\t[BACK]\n";
    for (Segment s : _backSegments)
      text += s + "\n";
    return text;
  }
}
