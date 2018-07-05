#ifndef DOCUMENTATION0_H
#define DOCUMENTATION0_H

/**
 * Encoding mode
 */
enum Mode {                                               //1
  /**
   * stereo encoding
   */
  STEREO = 0,                                             //2
  /**
   * joint frequency encoding
   */
  JOINT_STEREO,                                           //3
  /**
   * mono encoding
   */
  MONO                                                    //4
};

using RC = int;                                           //5 [undocumented]

RC init();                                                //6 [undocumented]

/**
 * Decode buffer of given length
 */
RC decode( unsigned char* buf, int len );                 //7

/**
 * Encode buffer of given length using the mode
 */
RC encode( unsigned char* buf, int len, Mode mode );      //8

#endif
