/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package biology;

/**
 * This enum describes the different ways that random selections can be specified
 * It was introduced Dec 2013 for use with DynamicRect 
 * 
 * SimData specification can specify parameters for any of 4 distributions
 * POISSON --- needs mean
 * UNIFORM ---- needs min and max
 * CONSTANT --- needs value
 * CDF --- needs a batch of functionpoints
 * @author pelikan
 */
public enum DISTRIBUTION{POISSON, UNIFORM, CONSTANT,CDF};
