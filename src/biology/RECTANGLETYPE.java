/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package biology;

/**
 *
 * @author pelikan
 * PLAIN = regular old rectangle
 * SPARSE = a fixed rectangle with only some sites viable
 *          it never updates
 * RANDOM = SPARSE rectangle that is generated anew at the start of
 *          each run
 * DYNAMIC = A rectangle that changes each year of each run.
 */
 public enum RECTANGLETYPE{PLAIN,SPARSE,DYNAMIC,RANDOM} ;  

