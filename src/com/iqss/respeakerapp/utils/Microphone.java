/**
* Microphone.java
*  
* Implements recording functionality----writing audio into WAV files.
*  
* @author Jessica Yao
*/

package com.iqss.respeakerapp.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

public class Microphone {
	private AudioRecord recorder = null;
	private Thread recordingThread = null;
	private boolean isRecording = false;
	
	private int fileSize = 0;
	private String filePath = null;	
	private String subFolder = null;	
	private RandomAccessFile randomAccessWriter = null;

	// should be compatible with all Android phones, but should cross-test
	public static final int RECORDER_SAMPLE_RATE = 44100;
	private static final int RECORDER_SOURCE = AudioSource.MIC;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static final int RECORDER_BUFFER_SIZE = AudioRecord
			.getMinBufferSize(RECORDER_SAMPLE_RATE, RECORDER_CHANNELS,
					RECORDER_AUDIO_ENCODING);

	public Microphone(String activityName) {
		// determines which folder to record to (i.e. if recording is the original or the respeaking)
		subFolder = (activityName.equals("RecordActivity")) ? "recordings" : "inProgress";
	}

	/*
	 * Sets up new file and writes WAV header if file doesn't already exist.
	 */
	public void setup(String filename) {
		if (setNewFileName(filename)){
			Log.d("Microphone", "New file created.");
			writeWavHeader();
			Log.d("Microphone", "WAV header created.");
		}
	}

	/*
	 * Starts recording by instantiating an AudioRecord instance.
	 */
	public void startRecording() {
		isRecording = true;
		if (recorder == null)
			recorder = new AudioRecord(RECORDER_SOURCE, RECORDER_SAMPLE_RATE,
					RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING,
					RECORDER_BUFFER_SIZE);
			Log.d("Microphone", "Recorder set up.");
		if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
			recorder.startRecording();		
			recordingThread = new Thread(new Runnable() {
				@Override
				public void run() {
					writeToFile();
				}
			});
			recordingThread.start();
		}
	}

	/* 
	 * Pausing recording if pause button pressed.
	 * (NOTE: recorder is not released although thread is stopped).
	 */
	public void pauseRecording() {
		isRecording = false;
		recorder.stop();
		recordingThread.interrupt();
		recordingThread = null;
	}
	
	/*
	 * Stopping recording if stop button pressed.
	 * (NOTE: calls pause recording in addition to releasing recorder instance).
	 */
	public void stopRecording() {
		stopRecorder();
		
		// move WAV file to respeakings folder if done recording respoken version
		if (subFolder.equals("inProgress") && filePath != null){
			subFolder = "respeakings";
			setNewFileName(new File(filePath).getName());
		}
	}
	
	/*
	 * Stops recorder and frees it, inserts file size.
	 */
	public void stopRecorder(){
		if (recorder != null) {
			if (recordingThread != null)
				pauseRecording();
			recorder.release();
			recorder = null;
			writeFileSize();
		}
	}

	/*
	 * Writes file size in WAV header (called only when recording stopped).
	 */
	private void writeFileSize() {
		try {
			RandomAccessFile sizeWriter = new RandomAccessFile(filePath, "rw");
			Log.d("Microphone", "File size: " + Integer.toString(fileSize));
			
			sizeWriter.seek(4); // write new file size + RIFF header size
			sizeWriter.writeInt(Integer.reverseBytes(fileSize + 36));

			sizeWriter.seek(40); // write new file size
			sizeWriter.writeInt(Integer.reverseBytes(fileSize));

			sizeWriter.close();
		} catch (Exception e) {
			Log.e("Microphone", "Error writing file size.");
		}
	}
	
	/*
	 * Write audio data into WAV file when recorder buffer overflows.
	 */
	private void writeToFile() {
		int readCode = 0;
		byte[] audioData = new byte[RECORDER_BUFFER_SIZE];
		BufferedOutputStream outputStream = null;
		try {		
			outputStream = new BufferedOutputStream(new FileOutputStream(filePath, true));
			while (isRecording) {
				readCode = recorder.read(audioData, 0, RECORDER_BUFFER_SIZE);
				if (readCode != AudioRecord.ERROR_INVALID_OPERATION) {
					try {
						outputStream.write(audioData);
						// increment fileSize by buffer size
						fileSize += RECORDER_BUFFER_SIZE; 
					} catch (IOException e) {
						Log.e("Microphone", "Error writing audio data to file.");
					}
				}
			}
		} catch (FileNotFoundException e) {
			Log.e("Microphone", "Output file not found.");
		}
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				Log.e("Microphone", "Error closing audiowriter.");
			}
		}
	}

	/*
	 * Creates new file in appropriate subFolder of respeakerApp.
	 * Returns true if file didn't exist before.
	 */
	private boolean setNewFileName(String filename) {
		File dir = new File(TabConstants.PREFIX + subFolder);
		dir.mkdirs();
		// moving file out of inProgress folder if done respeaking
		if (subFolder.equals("respeakings")){
			new File(filePath).renameTo(new File(dir, filename));
			return true;
		}
		// otherwise create file object to check if it exists
		File outputFile = new File(dir, filename);
		try {
			if (!outputFile.exists()){
				outputFile.createNewFile();
				filePath = outputFile.getAbsolutePath();
				fileSize = 0;
				return true;
			}
		} catch (IOException e) {
			Log.e("Microphone", "Error creating new file.");
		}
		
		filePath = outputFile.getAbsolutePath();
		
		// if recording wasn't completed (i.e. respeaking was paused halfway), gets previous fileSize
		try {
			RandomAccessFile sizeWriter = new RandomAccessFile(outputFile, "rw");
			sizeWriter.seek(40); // where size of raw data is located
			if (fileSize == 0)
				fileSize = Integer.reverseBytes(sizeWriter.readInt());
			sizeWriter.close();
			Log.d("Microphone", "Filesize: " + Integer.toString(fileSize));
		} catch (Exception e) {
			Log.e("Microphone", "Error reading file size");
		}
		return false;
	}

	/*
	 * Writes WAV header into new file.
	 */
	private void writeWavHeader() {
		try {
			randomAccessWriter = new RandomAccessFile(filePath, "rw");
			randomAccessWriter.setLength(0);
			randomAccessWriter.writeBytes("RIFF");
			randomAccessWriter.writeInt(0); // Final file size not known yet, write 0 
			randomAccessWriter.writeBytes("WAVE");
			randomAccessWriter.writeBytes("fmt ");
			randomAccessWriter.writeInt(Integer.reverseBytes(16)); // Sub-chunk size, 16 for PCM
			randomAccessWriter.writeShort(Short.reverseBytes((short) 1)); // AudioFormat, 1 for PCM
			randomAccessWriter.writeShort(Short.reverseBytes((short) 1));// Number of channels, 1 for mono, 2 for stereo
			randomAccessWriter.writeInt(Integer.reverseBytes(RECORDER_SAMPLE_RATE)); // Sample rate
			randomAccessWriter.writeInt(Integer.reverseBytes(RECORDER_SAMPLE_RATE * 16 * 1 / 8)); // Byte rate, SampleRate*NumberOfChannels*BitsPerSample/8
			randomAccessWriter.writeShort(Short.reverseBytes((short)(1*16/8))); // Block align, NumberOfChannels*BitsPerSample/8
			randomAccessWriter.writeShort(Short.reverseBytes((short) 16)); // Bits per sample
			randomAccessWriter.writeBytes("data");
			randomAccessWriter.writeInt(0); // Data chunk size not known yet, write 0
		} catch (Exception e) {
			Log.e("Microphone", "Error writing WAV header.");
		}		
	}
	
}