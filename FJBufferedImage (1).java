package cop5618;

import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;
import java.lang.*;
import javax.imageio.ImageIO;

import org.junit.BeforeClass;

public class FJBufferedImage extends BufferedImage {

   /** Instance variables **/
    int w;
	int h;
	Object data;
   ColorModel cm;
   WritableRaster raster;
   int nBands;
   int dataType;

   /**Constructors*/
	
	public FJBufferedImage(int width, int height, int imageType) {
		super(width, height, imageType);
		w = width;
		h = height;

		cm = ColorModel.getRGBdefault();
		raster =  cm.createCompatibleWritableRaster(w,
				h);
	}

	public FJBufferedImage(int width, int height, int imageType, IndexColorModel cm) {
		super(width, height, imageType, cm);
	}

	public FJBufferedImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied,
			Hashtable<?, ?> properties) {

		super(cm, raster, isRasterPremultiplied, properties);
		this.cm = cm;
		this.raster = raster;
	}
	

	/**
	 * Creates a new FJBufferedImage with the same fields as source.
	 * @param source
	 * @return
	 */
	public static FJBufferedImage BufferedImageToFJBufferedImage(BufferedImage source){
	       Hashtable<String,Object> properties=null; 
	       String[] propertyNames = source.getPropertyNames();
	       if (propertyNames != null) {
	    	   properties = new Hashtable<>();
	    	   for (String name: propertyNames){properties.put(name, source.getProperty(name));}
	    	   }
	 	   return new FJBufferedImage(source.getColorModel(), source.getRaster(), source.isAlphaPremultiplied(), properties);		
	}
	
	@Override
	public void setRGB(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize){
		/****IMPLEMENT THIS METHOD USING PARALLEL DIVIDE AND CONQUER*****/

		final int NPROCS =16;
		final ForkJoinPool pool = new ForkJoinPool(NPROCS);
		pool.invoke(new SetRGBFJ(0, 0, w, h, rgbArray, offset, w, NPROCS));
	}

	class SetRGBFJ extends RecursiveAction{
		private int xStart;
		private int yStart;
		private int w;
		private int h;
		private int offset;
		private int scanSize;
		int[] rgbArr;
		int arg;
		public SetRGBFJ(int xStart,int yStart,int w,int h,int[]rgbArr,int offset,int scanSize,int arg) {
			this.xStart = xStart;
			this.yStart = yStart;
			this.w = w;
			this.h = h;
			this.rgbArr = rgbArr;
			this.offset = offset;
			this.scanSize = scanSize;
			this.arg = arg;
		}

		@Override
		protected void compute() {

			if(arg < 2){
				FJBufferedImage.super.setRGB(xStart, yStart, w, h, rgbArr, offset, scanSize);
				return;
			}

			SetRGBFJ top = new SetRGBFJ(xStart,yStart,w,h-h/2,rgbArr,offset,w,arg/2);
			SetRGBFJ bottom = new SetRGBFJ(xStart,yStart+h/2,w,h-h/2,rgbArr,offset+(w*(h/2)),w,arg/2);
			top.fork();
			bottom.compute();
			top.join();
		}
	}



	/* getRGB implementation using forkJoinPool class */


	@Override
	public int[] getRGB(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize){
	       /****IMPLEMENT THIS METHOD USING PARALLEL DIVIDE AND CONQUER*****/

        final int NPROCS = 16;
		final ForkJoinPool pool = new ForkJoinPool(NPROCS);
        pool.invoke(new GetRGBFJ(0, 0, w, h, rgbArray, offset, w, NPROCS));
		return rgbArray;
	}

	class GetRGBFJ extends RecursiveAction{
		private int xStart;
        private int yStart;
        private int w;
		private int h;
		private int offset;
		private int scanSize;
		int[] rgbArr;
		int arg;
		public GetRGBFJ(int xStart,int yStart,int w,int h,int[]rgbArr,int offset,int scanSize,int arg) {
			this.xStart = xStart;
			this.yStart = yStart;
			this.w = w;
			this.h = h;
			this.rgbArr = rgbArr;
			this.offset = offset;
			this.scanSize = scanSize;
			this.arg = arg;
		}

		@Override
		protected void compute() {

			if(arg < 2){
				FJBufferedImage.super.getRGB(xStart, yStart, w, h, rgbArr, offset, scanSize);
				      return;
					}

			GetRGBFJ top = new GetRGBFJ(xStart,yStart,w,h/2,rgbArr,offset,w,arg/2);
            GetRGBFJ bottom = new GetRGBFJ(xStart,yStart+h/2,w,h-h/2,rgbArr,offset+(w*(h/2)),w,arg/2);
            top.fork();
			bottom.compute();
			top.join();
		}
	}


}
