package com.dsp.analyzer.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Configurations extends Properties {
  
  private static final long serialVersionUID = 201311161734L;

  public void load(String file) throws FileNotFoundException, IOException {
    load(new FileInputStream(file));
  }
  
  public double getDouble(String cname) {
    return Double.parseDouble(getProperty(cname, "0.0"));
  }
  
  public int getInt(String cname) {
    return Integer.parseInt(getProperty(cname, "0"));
  }
  
  public double[] getVector2d(String cname) {
    String value = getProperty(cname);
    double[] ret = {0.0, 0.0};
    if (value != null) {
      Matcher vec = Pattern.compile("\\[\\s*(\\d+.\\d+)\\s*,\\s*(\\d+.\\d+)\\s*\\]").matcher(value);
      if (!vec.matches()) return ret;
      ret[0] = Double.parseDouble(vec.group(1));
      ret[1] = Double.parseDouble(vec.group(2));
    }
    return ret;
  }
  
  public int[] getVector2i(String cname) {
    String value = getProperty(cname);
    int[] ret = {0, 0};
    if (value != null) {
      Matcher vec = Pattern.compile("\\[\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\]").matcher(value);
      if (!vec.matches()) return ret;
      ret[0] = Integer.parseInt(vec.group(1));
      ret[1] = Integer.parseInt(vec.group(2));
    }
    return ret;
  }
  
  // === Singleton Implementation
  private static Configurations _instance = new Configurations();
  
  private Configurations() { }

  public static Configurations getInstance() {
    return _instance;
  }
  // === Singleton Implementation
 
}
