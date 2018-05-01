package apps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

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

public class VideoAppLZW {

	public static void main(String[] args) throws IOException, InsufficientBitsLeftException {
		String base = "bunny";
		String filename="/Users/Samui/Documents/unc/spring 2018/comp590/hw/Huffman/raw/" + base +"/" + base + ".720p.yuv";
		File file = new File(filename);
		int width = 800;
		int height = 450;
		int num_frames = 150;
		int DICT_SIZE = 4096;


		InputStream message = new FileInputStream(file);

		File out_file = new File("/Users/Samui/Documents/unc/spring 2018/comp590/hw/Huffman/raw/" + base +"/" + base + "-compressed.dat");
		OutputStream out_stream = new FileOutputStream(out_file);

		System.out.println("Begin reading file");
		char[][][] raw_data = readFile(message, width, height, num_frames);
		System.out.println("Finished reading file");
		System.out.println("Begin reordering pixels");
		char[] ordered_data = order_spatial(raw_data, width, height, num_frames);
		System.out.println("Finished ordering pixels");

		System.out.println("Initializing encoder dictionary");
		int dictSizeEncoder = 256;
		Map<String, Integer> dictEncoder = new HashMap<String, Integer>();
		for (int i = 0; i < 256; i++) {
			dictEncoder.put("" + (char) i, i);
		}

		dictSizeEncoder++;    // codeword 256 is reserved for EOF

		System.out.println("Finished initializing encoder dictionary");
		System.out.println("Encoding file");

		String w = "";
		List<Integer> result = new ArrayList<Integer>();
		for (char c : ordered_data) {
			String wc = w + c;
			if (dictEncoder.containsKey(wc)) {
				w = wc;
			} else {
				result.add(dictEncoder.get(w));
				if(dictSizeEncoder < DICT_SIZE)
				    dictEncoder.put(wc, dictSizeEncoder++);
				w = "" + c;
			}
		}

		if (!w.equals("")){
			result.add(dictEncoder.get(w));
		}

		for(int k: result) {
			out_stream.write(k);
		}

		message.close();
		out_stream.close();

		System.out.println("Finished encoding file");

		System.out.println("Decompressing file");
		OutputStream decoded_file = new FileOutputStream(new File("/Users/Samui/Documents/unc/spring 2018/comp590/hw/Huffman/raw/" + base + "/" + base + "-decoded.dat"));

		int dictSizeDecoder = 256;
		Map<Integer, String> dictDecoder = new HashMap<Integer, String>();
		for (int i = 0; i < 256; i++) {
			dictDecoder.put(i, "" + (char) i);
		}
		dictSizeDecoder++;

		w = "" + (char)(int)result.remove(0);
		StringBuffer decoded_result = new StringBuffer(w);
		for (int k:result) {
			String entry;
			if (dictDecoder.containsKey(k))
				entry = dictDecoder.get(k);
			else if (k == dictSizeDecoder)
				entry = w + w.charAt(0);
			else
				throw new IllegalArgumentException("Bad compressed k: " + k);

			decoded_result.append(entry);

			if (dictSizeDecoder < DICT_SIZE) {
				dictDecoder.put(dictSizeDecoder++, w + entry.charAt(0));
			}

			w = entry;
		}

		char[] decoded_data = decoded_result.toString().toCharArray();
		for (char c : decoded_data) {
			decoded_file.write((int) c);
		}

		System.out.println("Finished decompressing file");

		decoded_file.close();

	}

	private static char[][][] readFile(InputStream src, int width, int height, int num_frames)
			throws IOException {
		char[][][] file_data = new char[num_frames][height][width];
		for (int i = 0; i<num_frames; i++) {
			System.out.println("Reading frame " + i);
			for (int j = 0; j<height; j++) {
				for (int k = 0; k < width; k++) {
					file_data[i][j][k] = (char) src.read();
				}
			}
		}

		return file_data;
	}

	private static char[] order_spatial(char[][][] data, int width, int height, int num_frames) {
		char[] ordered = new char[num_frames * height * width];
		for (int i = 0; i<num_frames; i++) {
			for (int j = 0; j<height; j++) {
				for (int k = 0; k < width; k++) {
					ordered[i * height * width + j * width + k] = data[i][j][k];
				}
			}
		}
		return ordered;
	}


	private static char[] order_temporal(char[][][] data, int width, int height, int num_frames) {
		char[] ordered = new char[num_frames * height * width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				for (int k = 0; k < num_frames; k++) {
					ordered[i * width * num_frames + j * num_frames + k] = data[k][i][j];
				}
			}
		}
		return ordered;
	}

	private static char[] compute_diff(char[] data) {
		char[] diff = new char[data.length];
		diff[0] = data[0];
		int d;
		for (int i = 1; i < data.length; i++) {
			d = (int) data[i] - (int) data[i-1];
			d = d % 256;
			if (d < 0) d+=256;
			diff[i] = (char) d;
		}
		return diff;
	}

	private static char[] reconstruct_diff(char[] diff) {
		char[] reconstruct = new char[diff.length];
		reconstruct[0] = diff[0];
		int previous;
		int current;
		for (int i = 1; i < diff.length; i++) {
			previous = (int) reconstruct[i-1];
			current = (int) diff[i] + previous;
			current = current % 256;
			reconstruct[i] = (char) current;
		}
		return reconstruct;
	}





}
