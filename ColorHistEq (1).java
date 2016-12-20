package cop5618;


import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.*;
import java.awt.Color;
import java.util.stream.Collectors;
public class ColorHistEq {

    //Use these labels to instantiate you timers.  You will need 8 invocations of now()
	static String[] labels = { "getRGB", "convert to HSB", "create brightness map", "probability array",
			"parallel prefix", "equalize pixels", "setRGB" };

	static final int NUMBINS = 255;
	static final float NUMBINSF = 1.0f * NUMBINS;


	static Timer colorHistEq_serial(BufferedImage image, BufferedImage newImage) {
		//*** Image properties *** //
		int w= image.getWidth();
		int h = image.getHeight();
		ColorModel colorModel = ColorModel.getRGBdefault();
		///*************************//


		Timer times = new Timer(labels);
		times.now();

		//**** Get the RGBArray from the image *******//
		int[] RGB = image.getRGB(0, 0, w, h, new int[w * h], 0, w);
		times.now();
		//*******************************************//



		//**** Get the HSBArray from the RGBArray *******//
		float[][] HSBArr = Arrays.stream(RGB)
				                 .mapToObj(pixel -> {
									 return Color.RGBtoHSB(colorModel.getRed(pixel),
											               colorModel.getGreen(pixel),
											               colorModel.getBlue(pixel), null);
				                                    })
				                 .toArray(float[][]::new);

		times.now();
		// *******************************************//



		//**** Brightness Map from the HSBArray *******//
		Map<Integer,Long> map = Arrays.stream(HSBArr)
				.map(eachHSB -> (int)Math.floor(NUMBINS * eachHSB[2]))
				.collect(Collectors.groupingBy(brightness -> brightness, Collectors.counting()));
		times.now();
		// ******************************************************//


		//**** Probability array from  Brightness Map *******//
		Long[] arr = map.values()
				.stream()
				.toArray(Long[]::new);
		times.now();

		//**** Parallel prefix Sum for the array *******//
		Arrays.parallelPrefix(arr, Long::sum);
		times.now();
		//********************************************//

		//**** RGBArray after adjusting the brightness *******//
		int[] RGBArr = Arrays.stream(HSBArr)
				.mapToInt(each -> {
					int binNum = (int) Math.floor(NUMBINS * each[2]);
					Long val = arr[binNum];
					long numPixel = w * h;
					float brightness = 1.0f * val / numPixel;
					return Color.HSBtoRGB(each[0], each[1], brightness);
				})
				.toArray();
		times.now();
		//********************************************//

		//**** Set the new image to the RGBArr *******//
		newImage.setRGB(0, 0, w, h, RGBArr, 0, w);
		times.now();
		//****************************************//

		return times;
	}





	//////////////
	///////////
    ////////
	//// PARALLEL EXECUTION USING FORK JOIN AND PARALLEL STREAM
	////////
	//////////
	////////////






	static Timer colorHistEq_parallel(FJBufferedImage image, FJBufferedImage newImage) {


        //*** Image properties *** //
		int w= image.getWidth();
		int h = image.getHeight();
		ColorModel colorModel = ColorModel.getRGBdefault();
		///*************************//


		Timer times = new Timer(labels);
		times.now();

		//**** Get the RGBArray from the image *******//
		int[] RGB = image.getRGB(0, 0, w, h, new int[w * h], 0, w);
		times.now();
        //*******************************************//



		//**** Get the HSBArray from the RGBArray *******//
		float[][] HSBArr = Arrays.stream(RGB)
				.parallel()
				.mapToObj(pixel -> {
					return Color.RGBtoHSB(colorModel.getRed(pixel),
							colorModel.getGreen(pixel),
							colorModel.getBlue(pixel), null);
								 })
				                 .toArray(float[][]::new);

		times.now();
        // *******************************************//



		//**** Brightness Map from the HSBArray *******//
		Map<Integer,Long> map = Arrays.stream(HSBArr)
				.parallel()
				.map(eachHSB -> (int)Math.floor(NUMBINS * eachHSB[2]))
				.collect(Collectors.groupingBy(brightness -> brightness, Collectors.counting()));
		times.now();
        // ******************************************************//


		//**** Probability array from  Brightness Map *******//
		Long[] arr = map.values()
				        .stream()
				        .parallel()
				        .toArray(Long[]::new);
		times.now();

		//**** Parallel prefix Sum for the array *******//
		Arrays.parallelPrefix(arr, Long::sum);
		times.now();
		//********************************************//

		//**** RGBArray after adjusting the brightness *******//
		int[] RGBArr = Arrays.stream(HSBArr)
				.parallel()
				.mapToInt(each -> {
					int binNum = (int) Math.floor(NUMBINS * each[2]);
					Long val = arr[binNum];
					long numPixel = w * h;
					float brightness = 1.0f * val / numPixel;
					return Color.HSBtoRGB(each[0], each[1], brightness);
							 })
							.toArray();
		times.now();
        //********************************************//

		//**** Set the new image to the RGBArr *******//
		newImage.setRGB(0, 0, w, h, RGBArr, 0, w);
		times.now();
		//****************************************//

		return times;
	}

}
