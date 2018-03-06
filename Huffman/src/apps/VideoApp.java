package apps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import models.Unsigned8BitModel;
import codec.HuffmanEncoder;
import codec.SymbolDecoder;
import codec.SymbolEncoder;
import models.Symbol;
import models.SymbolModel;
import models.Unsigned8BitModel.Unsigned8BitSymbol;
import io.InsufficientBitsLeftException;
import io.BitSink;
import io.BitSource;
import codec.ArithmeticDecoder;
import codec.ArithmeticEncoder;
import codec.HuffmanDecoder;
import io.InputStreamBitSource;
import io.OutputStreamBitSink;

public class VideoApp {

	private static long[] counts = new long[] {
			33640551, 3102185, 1839985, 1160834, 793904, 577031, 439784, 382699, 253751, 204650, 
			169293, 141833, 118681, 101223, 97515, 72203, 60275, 52968, 44892, 40554, 35716, 
			34885, 27597, 22806, 20226, 17417, 16385, 14588, 14396, 12334, 10626, 9479, 8120, 8347, 
			7782, 8582, 7626, 6754, 6633, 4721, 5938, 5716, 5940, 5738, 5142, 5436, 3203, 5161, 
			5264, 5455, 5315, 4881, 5050, 2559, 5175, 5409, 5172, 5299, 5001, 5205, 1992, 5087, 
			5085, 5209, 5359, 4989, 4900, 1699, 4753, 4672, 4762, 4938, 4624, 4589, 1435, 4671, 4542, 
			4527, 4615, 4217, 4289, 1290, 4174, 3782, 3856, 4060, 3585, 3671, 3546, 1106, 3413, 
			3420, 3616, 3226, 3242, 3086, 1102, 3156, 3138, 3179, 3095, 3003, 2962, 934, 2977, 3000, 
			3026, 3078, 2994, 3002, 936, 2963, 3014, 3034, 3099, 2893, 3011, 1059, 2920, 2936, 3056, 
			3106, 3052, 3057, 1070, 3091, 3038, 3173, 3223, 3207, 3130, 1087, 3269, 3226, 3260, 
			3388, 3364, 3286, 1089, 3360, 3267, 3302, 3429, 3196, 3301, 1218, 3282, 3365, 3219, 3500, 
			3365, 3352, 1267, 3472, 3462, 3405, 3881, 3768, 3611, 1285, 4025, 3892, 3944, 3962, 
			3988, 3654, 1265, 3686, 3621, 3548, 3599, 3843, 3591, 3429, 1392, 3482, 3483, 3498, 
			3694, 3497, 3317, 1569, 3394, 3383, 3358, 3592, 3403, 3302, 1865, 3440, 3329, 3294, 3560, 
			3356, 3279, 2151, 3289, 3329, 3368, 3701, 3501, 3466, 2865, 3675, 3740, 3749, 4304, 
			4331, 4118, 3748, 4425, 4667, 4702, 5547, 5638, 5500, 5554, 6055, 6457, 6687, 7900, 
			8874, 8866, 9085, 10167, 12070, 13103, 16947, 18874, 18334, 18072, 22002, 25638, 27816, 
			30864, 37931, 38382, 42804, 49088, 56691, 64123, 76883, 102905, 105159, 123575, 144168, 
			170034, 202767, 248937, 370492, 415757, 547352, 749984, 1101381, 1779517, 3082706 
	};

	public static void main(String[] args) throws IOException, InsufficientBitsLeftException {
		String base = "bunny";
		String filename="/Users/kmp/tmp/" + base + ".450p.yuv";
		File file = new File(filename);
		int width = 800;
		int height = 450;
		int num_frames = 150;


		Unsigned8BitModel model = new Unsigned8BitModel(counts);

		//		InputStream training_values = new FileInputStream(file);
		int[][] current_frame = new int[width][height];

		//		for (int f=0; f < num_frames; f++) {
		//			System.out.println("Training frame difference " + f);
		//			int[][] prior_frame = current_frame;
		//			current_frame = readFrame(training_values, width, height);
		//
		//			int[][] diff_frame = frameDifference(prior_frame, current_frame);
		//			trainModelWithFrame(model, diff_frame);
		//		}
		//		training_values.close();		

		//		HuffmanEncoder encoder = new HuffmanEncoder(model, model.getCountTotal());
		//		Map<Symbol, String> code_map = encoder.getCodeMap();

		SymbolEncoder encoder = new ArithmeticEncoder(model);

		Symbol[] symbols = new Unsigned8BitSymbol[256];
		for (int v=0; v<256; v++) {
			SymbolModel s = model.getByIndex(v);
			Symbol sym = s.getSymbol();
			symbols[v] = sym;

			long prob = s.getProbability(model.getCountTotal());
			System.out.println("Symbol: " + sym + " probability: " + prob + "/" + model.getCountTotal());
		}			

		InputStream message = new FileInputStream(file);

		File out_file = new File("/Users/kmp/tmp/" + base + "-compressed.dat");
		OutputStream out_stream = new FileOutputStream(out_file);
		BitSink bit_sink = new OutputStreamBitSink(out_stream);

		current_frame = new int[width][height];

		for (int f=0; f < num_frames; f++) {
//		for (int f=0; f < 2; f++) {
			System.out.println("Encoding frame difference " + f);
			int[][] prior_frame = current_frame;
			current_frame = readFrame(message, width, height);

			int[][] diff_frame = frameDifference(prior_frame, current_frame);
			encodeFrameDifference(diff_frame, encoder, bit_sink, symbols);
		}

		message.close();
		//		bit_sink.padToWord();
		((ArithmeticEncoder) encoder).close(bit_sink);
		out_stream.close();

		BitSource bit_source = new InputStreamBitSource(new FileInputStream(out_file));
		OutputStream decoded_file = new FileOutputStream(new File("/Users/kmp/tmp/" + base + "-decoded.dat"));

		//		SymbolDecoder decoder = new HuffmanDecoder(encoder.getCodeMap());
		SymbolDecoder decoder = new ArithmeticDecoder(model);

		current_frame = new int[width][height];

		for (int f=0; f<num_frames; f++) {
		// for (int f=0; f<2; f++) {
			System.out.println("Decoding frame " + f);
			int[][] prior_frame = current_frame;
			int[][] diff_frame = decodeFrame(decoder, bit_source, width, height);
			current_frame = reconstructFrame(prior_frame, diff_frame);
			outputFrame(current_frame, decoded_file);
		}

		decoded_file.close();

	}

	private static int[][] readFrame(InputStream src, int width, int height) 
			throws IOException {
		int[][] frame_data = new int[width][height];
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				frame_data[x][y] = src.read();
			}
		}
		return frame_data;
	}

	private static int[][] frameDifference(int[][] prior_frame, int[][] current_frame) {
		int width = prior_frame.length;
		int height = prior_frame[0].length;

		int[][] difference_frame = new int[width][height];

		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				difference_frame[x][y] = ((current_frame[x][y] - prior_frame[x][y])+256)%256;
			}
		}
		return difference_frame;
	}

	private static void trainModelWithFrame(Unsigned8BitModel model, int[][] frame) {
		int width = frame.length;
		int height = frame[0].length;
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				model.train(frame[x][y]);
			}
		}
	}

	private static void encodeFrameDifference(int[][] frame, SymbolEncoder encoder, BitSink bit_sink, Symbol[] symbols) 
			throws IOException {

		int width = frame.length;
		int height = frame[0].length;

		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				encoder.encode(symbols[frame[x][y]], bit_sink);
			}
		}
	}

	private static int[][] decodeFrame(SymbolDecoder decoder, BitSource bit_source, int width, int height) 
			throws InsufficientBitsLeftException, IOException {
		int[][] frame = new int[width][height];
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				frame[x][y] = ((Unsigned8BitSymbol) decoder.decode(bit_source)).getValue();
			}
		}
		return frame;
	}

	private static int[][] reconstructFrame(int[][] prior_frame, int[][] frame_difference) {
		int width = prior_frame.length;
		int height = prior_frame[0].length;

		int[][] frame = new int[width][height];
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				frame[x][y] = (prior_frame[x][y] + frame_difference[x][y])%256;
			}
		}
		return frame;
	}

	private static void outputFrame(int[][] frame, OutputStream out) 
			throws IOException {
		int width = frame.length;
		int height = frame[0].length;
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				out.write(frame[x][y]);
			}
		}
	}
}
