/*
 * ============================================================================
 * JGribX
 * ============================================================================
 * Written by Andrew Spiteri <andrew.spiteri@um.edu.mt>
 * Adapted from JGRIB: http://jgrib.sourceforge.net/
 * 
 * Licensed under MIT: https://github.com/spidru/JGribX/blob/master/LICENSE
 * ============================================================================
 */
package mt.edu.um.cf2.jgribx;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class is an input stream wrapper that can read a specific number of
 * bytes and bits from an input stream.
 *
 * @author  Benjamin Stark
 * @version 1.0
 */

public class GribInputStream extends FilterInputStream
{

   /**
    * Buffer for one byte which will be processed bit by bit.
    */
   protected int bitBuf = 0;

   /**
    * Current bit position in <tt>bitBuf</tt>.
    */
   protected int bitPos = 0;
   
   /**
    * A byte counter which can be used to keep track of the number of bytes read
    */
//   private int countBytes;
   
   private long countBits;
   
   private long markedCountBits;

   /** Counter keeping track of bit offset relative to start of file */
   private long absoluteBitPosition;


   /**
    * Constructs a bit input stream from an <tt>InputStream</tt> object.
    *
    * @param in input stream that will be wrapped
    */
   public GribInputStream(InputStream in)
   {
      super(in);
   }
   
   @Override
   public synchronized void mark(int readLimit)
   {
       super.mark(readLimit);
       markedCountBits = countBits;
   }
   
   @Override
   public synchronized void reset() throws IOException
   {
       super.reset();
       if (countBits < markedCountBits)
       {
           Logger.println("Unexpected state countBits (" + countBits + ") < markedCountBits (" + markedCountBits + ")", Logger.WARNING);
       }
       absoluteBitPosition -= (countBits - markedCountBits);
       countBits = markedCountBits;
   }
   
   public long getBitCounter()
   {
       return countBits;
   }
   
   public int getByteCounter()
   {
       return (int) Math.ceil((double) countBits / 8);
   }

   public long getAbsoluteBytePosition()
   {
       return (long) Math.ceil((double) absoluteBitPosition / 8);
   }
   
   public void resetBitCounter()
   {
       countBits = 0;
   }
   
    public void seekNextByte()
    {
        if (bitPos != 0)
        {
            countBits += bitPos;
            absoluteBitPosition += bitPos;
            bitPos = 0;
        }
    }

   /**
    * Reads the specified number of bytes from the input stream and converts the byte array into an unsigned integer.
    * @param nBytes     the number of bytes to read
    * @return the unsigned integer value corresponding to the byte array
    * @throws IOException 
    */
   public int readUINT(int nBytes) throws IOException
   {
       byte data[] = read(nBytes);
       return Bytes2Number.bytesToUint(data);
   }
   
   public int readINT(int nBytes, int format) throws IOException
   {
       byte data[] = read(nBytes);
       return Bytes2Number.bytesToInt(data, format);
   }
   
   public float readFloat(int nBytes, int format) throws IOException
   {
       byte data[] = read(nBytes);
       return Bytes2Number.bytesToFloat(data, format);
   }
   
   /**
    * Read an unsigned 8 bit value.
    *
    * @return unsigned 8 bit value as integer
    * @throws IOException 
    */
   public int readUI8() throws IOException
   {

      int ui8 = in.read();

      if (ui8 < 0)
         throw new IOException("End of input.");
      
      return ui8;
   }


   /**
    * Read specific number of unsigned bytes from the input stream.
    *
    * @param length number of bytes to read and return as integers
    *
    * @return unsigned bytes as integer values
    * @throws IOException 
    */
   public int[] readUI8(int length) throws IOException
   {

      int[] data = new int[length];
      int read = 0;

      for (int i = 0; i < length && read >= 0; i++)
         data[i] = read = this.read();

      if (read < 0)
         throw new IOException("End of input.");

      return data;
   }


    /**
     * Read specific number of bytes from the input stream.
     *
     * @param length number of bytes to read
     *
     * @return array of read bytes
     * @throws IOException 
     */
    public byte[] read(int length) throws IOException
    {

       byte[] data = new byte[length];

       int numRead = this.read(data);

       if (numRead < length)
       {

          // retry reading
          int numReadRetry = this.read(data, numRead, data.length - numRead);

          if (numRead + numReadRetry < length)
          {
              throw new IOException("Unexpected end of input.");
          }
       }

       return data;
    }
   
    @Override
    public int read() throws IOException
    {
        int value = super.read();
        countBits += 8;
        absoluteBitPosition += 8;
        return value;
    }
       
    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        int i = super.read(b, off, len);
        countBits += (i * 8L);
        absoluteBitPosition += (i * 8L);
        return i;
    }
    
   @Override
    public long skip(long n) throws IOException
    {
        long nBytesSkipped = super.skip(n);
        countBits += (nBytesSkipped * 8);
        absoluteBitPosition += (nBytesSkipped * 8);
        return nBytesSkipped;
    }
   
   /**
    * Read an unsigned value from the given number of bits.
    *
    * @param numBits number of bits used for the unsigned value
    *
    * @return value read from <tt>numBits</tt> bits as long
    * @throws IOException 
    */
   public long readUBits(int numBits) throws IOException
   {

      if (numBits == 0) return 0;
      
      countBits += numBits;
      absoluteBitPosition += numBits;

      int bitsLeft = numBits;
      long result = 0;

      if (this.bitPos == 0)
      {

         this.bitBuf = in.read();
         this.bitPos = 8;
      }

      while (true)
      {

         int shift = bitsLeft - this.bitPos;
         if (shift > 0)
         {

            // Consume the entire buffer
            result |= this.bitBuf << shift;
            bitsLeft -= this.bitPos;

            // Get the next byte from the input stream
            this.bitBuf = in.read();
            this.bitPos = 8;
         }
         else
         {

            // Consume a portion of the buffer
            result |= this.bitBuf >> -shift;
            this.bitPos -= bitsLeft;
            this.bitBuf &= 0xff >> (8 - this.bitPos);   // mask off consumed bits

            return result;
         }
      }
   }


   /**
    * Read a signed value from the given number of bits
    *
    * @param numBits number of bits used for the signed value
    *
    * @return value read from <tt>numBits</tt> bits as integer
    * @throws IOException 
    */
   public int readSBits(int numBits) throws IOException
   {

      // Get the number as an unsigned value.
      long uBits = readUBits(numBits);

      // Is the number negative?
      if ((uBits & (1L << (numBits - 1))) != 0)
      {

         // Yes. Extend the sign.
         uBits |= -1L << numBits;
      }

      return (int) uBits;
   }

    /**
     * Seek the input stream for the specified pattern.
     * The seek functionality is implemented by reading out chunks of data at a time.
     * Successive chunks overlap by {@code pattern.length} bytes to ensure the full pattern will appear in a chunk.
     * If the pattern is found, the next byte in the input stream is the byte directly following the pattern.
     * @param pattern The pattern for which to seek
     * @param consumePattern Determines whether the pattern is consumed or not
     * @return Number of bytes consumed from the input stream
     * @throws IOException
     */
   public int seekBytePattern(byte[] pattern, boolean consumePattern) throws IOException
   {
       boolean endPatternFound = false;
       int iChunkByte = 0;
       int nAvailableBytes = 0;
       int iStartPos = this.getByteCounter();
       final int N_BYTES_PER_CHUNK = 1000;

       assert pattern.length > 0 && pattern.length < N_BYTES_PER_CHUNK;

       while ((nAvailableBytes = this.available()) > 0)
       {
           // Limit the number of bytes to be read
           if (nAvailableBytes > N_BYTES_PER_CHUNK)
           {
               nAvailableBytes = N_BYTES_PER_CHUNK;
           }

           this.mark(nAvailableBytes);
           int[] bytes = this.readUI8(nAvailableBytes);
           iChunkByte = 0;

           // Search for pattern within chunk
           while (!endPatternFound && iChunkByte <= nAvailableBytes - pattern.length)
           {
               endPatternFound = true;
               // Compare next bytes with pattern
               for (int iPattern = 0; iPattern < pattern.length; iPattern++)
               {
                   if (bytes[iChunkByte + iPattern] != pattern[iPattern])
                   {
                       endPatternFound = false;
                       iChunkByte++;
                       break;
                   }
               }
           }

           // Backtrack stream position until it's just before/after the last byte of the pattern
           this.reset();
           int offset = consumePattern ? pattern.length : 0;
           if (this.skip(iChunkByte + offset) != iChunkByte + offset)
           {
               throw new IOException("Skipped less bytes than expected");
           }

           if (endPatternFound) break;
       }

       if (!endPatternFound)
       {
           throw new EOFException("Reached end of stream without finding pattern");
       }

       // Calculate amount of bytes consumed
       int iEndPos = this.getByteCounter();

       return iEndPos - iStartPos;
   }

}

