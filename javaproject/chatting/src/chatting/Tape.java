/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatting;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;

public class Tape {

	AudioFormat af = null;
	TargetDataLine td = null;
	ByteArrayInputStream bais = null;
	ByteArrayOutputStream baos = null;
	AudioInputStream ais = null;
	Boolean stopflag = false;
	
	public void capture()
	{
		try {
			af = getAudioFormat();
			DataLine.Info info = new DataLine.Info(TargetDataLine.class,af);
			td = (TargetDataLine)(AudioSystem.getLine(info));
			td.open(af);
			td.start();
			Record record = new Record();
			Thread t1 = new Thread(record);
			t1.start();
			
		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
			return;
		}
	}

	public void save()
	{
            stopflag = true;	
        af = getAudioFormat();
     
        byte audioData[] = baos.toByteArray();
        bais = new ByteArrayInputStream(audioData);
        ais = new AudioInputStream(bais,af, audioData.length / af.getFrameSize());
        File file = null;
        try {	
        	 file = new File(this.getClass().getResource("/").getPath()+"sound.wav");   
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
        	try {
        		
        		if(bais != null)
        		{
        			bais.close();
        		} 
        		if(ais != null)
        		{
        			ais.close();		
        		}
			} catch (Exception e) {
				e.printStackTrace();
			}   	           
        }
	}
	public AudioFormat getAudioFormat() 
	{
		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED ;
		float rate = 8000f;
		int sampleSize = 16;
		boolean bigEndian = true;
		int channels = 1;
		return new AudioFormat(encoding, rate, sampleSize, channels,
				(sampleSize / 8) * channels, rate, bigEndian);
	}
	class Record implements Runnable
	{
		byte bts[] = new byte[10000];
		public void run() {	
			baos = new ByteArrayOutputStream();		
			try {
				System.out.println("开始录音");
				stopflag = false;
				while(stopflag != true)
				{
					int cnt = td.read(bts, 0, bts.length);
					if(cnt > 0)
					{
						baos.write(bts, 0, cnt);
					}
				} 
                              
			} catch (Exception e) {
				e.printStackTrace();
			}
                        finally{                      				
                            try 
                            {
					if(baos != null)
					{   
                                            baos.close();
                                        } 
                            } 
                             catch (IOException ex) {
                                 Logger.getLogger(Tape.class.getName()).log(Level.SEVERE, null, ex);
				}	      
                                 System.out.println("结束录音");
			        td.close();	
                                        
				
			}
		}
		
	}
	
}