/*
The MIT License (MIT)

Copyright (c) 2015 Luna Winters

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

import java.applet.Applet;
import java.awt.Color;
import java.awt.Event;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

public class M extends Applet implements Runnable
{
	/**
	 *
	 */
	private static final long serialVersionUID = 2540241766738521625L;

	/**
	 * Hex 4k gon
	 */

	private Thread thread;

	// Globals
	private static int W = 800;
	private static int H = 600;
	private static float NANOTIME = 1000000000;

	// INPUT
	private static final int LEFT = Event.LEFT;
	private static final int RIGHT = Event.RIGHT;
	private static final int UP = Event.UP;

	// COLORS
	private static final int COLOR_BRIGHT_RED = 0;
	private static final int COLOR_DARK_RED = 1;
	private static final int COLOR_FORE_RED = 2;
	private static final int COLOR_WHITE = 3;
	private static final int COLOR_BLACK = 4;

	private static final int COLOR_BRIGHT_BLU = 5;
	private static final int COLOR_DARK_BLU = 6;
	private static final int COLOR_FORE_BLU = 7;


	//PATTERNS
	private static final byte[] p1 = {0x2A,0x00,0x00,0x2A,0x00,0x00,0x3D,0x00};
	private static final byte[] p2 = {0x2A,0x00,0x00,0x00,0x3D,0x00};
	private static final byte[] p3 = {0x00,0x00,0x00,0x00,0x3D,0x00};
	private static final byte[] p4 = {0x2F,0x00,0x00,0x00,0x3D,0x00,0x00,0x00,0x2F,0x00};
	private static final byte[] p5 = {0x27,0x07,0x0E,0x1C,0x38,0x39,0x00};
	private static final byte[] p6 = {0x39,0x38,0x1C,0x0E,0x07,0x27,0x00,0x00,0x00};
	private static final byte[] p7 = {0x2F,0x07,0x07,0x02,0x12,0x12,0x38,0x38,0x3D,0x3D,0x00};
	private static final byte[] p8 = {0x1B,0x09,0x09,0x2D,0x09,0x09,0x1B,0x09,0x09,0x00};
	//Hyper
	private static final byte[] p9h =  {0x2D,0x00,0x00,0x1B,0x00,0x00,0x36,0x00,0x00,0x2D,0x00};
	private static final byte[] p10h = {0x07,0x2F,0x2F,0x02,0x02,0x12,0x3A,0x3A,0x02,0x02,0x07,0x07,0x00};


	// MOUSE AND KEYBOARD STATE
	private static final int[] key = new int[1024];

	private static final Random ra = new Random();

	@Override
	public void start()
	{
		new Thread(this).start();
	}

	public void run()
	{
		try
		{
			if (thread == null)
			{
				thread = new Thread(this);
				thread.start();

				// Setting up graphics
				final BufferedImage screen = new BufferedImage(W, H,
						BufferedImage.TYPE_INT_RGB);
				final Graphics g = screen.getGraphics();
				final Graphics appletGraphics = getGraphics();

				// Colors
				final Color[] color = new Color[str_colors.length() >> 1];
				int i,j,k;
				long ll;

				for (i = 0; i < color.length; i++)
				{
					color[i] = new Color((str_colors.charAt(2 * i + 0) << 16)
							+ str_colors.charAt(2 * i + 1));
				}
				int detectionpx;
				// Some variables to use for the fps.
				float dt, lastTime = System.nanoTime() / NANOTIME;
				// Game floats
				float rotoffset = 0f;
				float playerRot = 0f;
				double em = 0;
				long eNum = 0;
				int gameScreen = 0; //0-Start 1-Game 2-End 3-Credits
				float score = 0;
				float best = 0;
				// Polygon classes
				Polygon poly = new Polygon();
				Point pnt = new Point();
				long[] st = new long[7]; //Start positions of enemies
				byte[][] bt = new byte[st.length][]; //Enemy pattern

				for(i=0;i<st.length;i++)
				{
					switch(ra.nextInt(10))
					{
						case 0: bt[i] = p1; break;
						case 1: bt[i] = p2; break;
						case 2: bt[i] = p3; break;
						case 3: bt[i] = p4; break;
						case 4: bt[i] = p5; break;
						case 5: bt[i] = p6; break;
						case 6: bt[i] = p7; break;
						case 7: bt[i] = p8; break;
						case 8: bt[i] = p9h; break;
						case 9: bt[i] = p10h; break;
					}
					st[i]+=bt[i].length+5;
					j=i;
					while(++j<st.length)st[j]=st[i];
				}

				// Game loop.
				while (isActive())
				{
					final float time = System.nanoTime() / NANOTIME;
					dt = time - lastTime;
					dt = dt > 0.05f ? 0.05f : dt;
					dt = dt < 0.01f ? 0.01f : dt;
					lastTime = time;
					/************
					 * INPUT
					 ************/
					if (key[LEFT] == 1)
					{
						playerRot -= score<60?4f:6f;
					}
					if (key[RIGHT] == 1)
					{
						playerRot += score<60?4f:6f;
					}
					if (playerRot <= 0) playerRot += 360;
					if (playerRot >= 360) playerRot -= 360;
					/************
					 * RENDER
					 ************/


					if(gameScreen ==0) //Start Screen
					{
						/*
						 * BACKGROUND
						 */
						for(i=0;i<6;i++)
						{
							if(i%2==0)g.setColor(color[(score<60)?COLOR_BRIGHT_RED:COLOR_BRIGHT_BLU]);
							else g.setColor(color[(score<60)?COLOR_DARK_RED:COLOR_DARK_BLU]);
							poly = setPoly(3,500,rotoffset-(i*60),0,-500);
							g.fillPolygon(poly);
							poly.reset();
						}
						g.setColor(color[COLOR_WHITE]);
						g.setFont(new Font("Serif",Font.BOLD,45));
						g.drawString("HEX4kGON", W/2-130, H/2-225);
						g.drawString("PRESS UP", W/2-100, H/2);
						if(key[UP] == 1) gameScreen = 1;
					}
					else if (gameScreen ==1) //Game
					{
						score+=dt;
						rotoffset += dt * ((score<60)?60f:100f);
						rotoffset = rotoffset >= 360 ? rotoffset - 360 : rotoffset;
						em -= dt*((score<60)?60d:120d);
						/*
						 * BACKGROUND
						 */
						for(i=0;i<6;i++)
						{
							if(i%2==0)g.setColor(color[(score<60)?COLOR_BRIGHT_RED:COLOR_BRIGHT_BLU]);
							else g.setColor(color[(score<60)?COLOR_DARK_RED:COLOR_DARK_BLU]);
							poly = setPoly(3,500,rotoffset-(i*60),0,-500);
							g.fillPolygon(poly);
							poly.reset();
						}
						/* ENEMIES */

						//Reset if below value
						for(k=0;k<st.length;k++)
						{
							if(st[k]+bt[k].length < eNum)
							{
								switch(ra.nextInt(10))
								{
									case 0: bt[k] = p1; break;
									case 1: bt[k] = p2; break;
									case 2: bt[k] = p3; break;
									case 3: bt[k] = p4; break;
									case 4: bt[k] = p5; break;
									case 5: bt[k] = p6; break;
									case 6: bt[k] = p7; break;
									case 7: bt[k] = p8; break;
									case 8: bt[k] = p9h; break;
									case 9: bt[k] = p10h; break;
								}
								st[k] = 0;
								for(long z : st) if(st[k]<z)st[k]=z;
								st[k]+=bt[k].length+5;
							}
						}
						/* ENEMY RENDER */
						g.setColor(color[(score<60)?COLOR_FORE_RED:COLOR_FORE_BLU]);
						for (i = 0; i < 6; i++)
						{
							for(ll=eNum;ll<eNum+35;ll++)
							{
								for(j=0;j<4;j++)
								{
									float hi = W/2f/25f*(1f+0.5f);
									float os = W/2f/20f*(1f+0.5f);
									float wi = 246f;

									pnt.y = (int) ((H / 2) + (hi * Math.cos(Math.toRadians(45f)+j * 2f * Math.PI
											/ 4f))+ (ll+1)*os + em);

									wi *= ((float)pnt.y/(H)-0.5f)*2f;
									if(j==3&&wi<=0f)eNum++;

									pnt.x = (int) ((W / 2) + (wi * Math.sin(Math.toRadians(45f)+j * 2f * Math.PI
											/ 4f)));

									AffineTransform.getRotateInstance(
											Math.toRadians(-rotoffset - (i*60)),
											W / 2, H / 2).transform(pnt, pnt);
									poly.addPoint(pnt.x, pnt.y);
								}

								boolean shouldFill = false;

								for(k=0;k<st.length;k++)
								{
									j = (int) (ll-st[k]);
									if(j <bt[k].length && j>=0)
									{
										shouldFill = ((bt[k][j]>>i)&1)!=0;
									}
								}
								if(shouldFill)g.fillPolygon(poly);
								poly.reset();
							}
						}
						/* CENTER HEXAGON */
						g.setColor(color[(score<60)?COLOR_FORE_RED:COLOR_FORE_BLU]);
						poly = setPoly(6,40,rotoffset-90,0,0);
						g.fillPolygon(poly);
						poly.reset();
						/* Hexagon inside */
						g.setColor(color[(score<60)?COLOR_DARK_RED:COLOR_DARK_BLU]);
						poly = setPoly(6,35,rotoffset-90,0,0);
						g.fillPolygon(poly);
						poly.reset();
						/* Cursor */
						g.setColor(color[(score<60)?COLOR_FORE_RED:COLOR_FORE_BLU]);
						poly = setPoly(3,8,rotoffset-playerRot+180,0,50);
						g.fillPolygon(poly);
						poly.reset();
						/* Time */
						g.setColor(color[COLOR_BLACK]);
						for(i=0;i<4;i++)
						{
							pnt.x = (int) ((W / 2) + (150 * Math.sin(i * 2 * Math.PI/4)));
							pnt.y = (int) ((H / 2) + (150 * Math.cos(i * 2 * Math.PI/4)));
							AffineTransform.getRotateInstance(
									Math.toRadians(45), W / 2, H / 2)
									.transform(pnt, pnt);
							pnt.x += (W/2)-100;
							pnt.y -= (H/2)+50;
							poly.addPoint(pnt.x, pnt.y);
						}
						g.fillPolygon(poly);
						poly.reset();

						g.setColor(color[COLOR_WHITE]);
						g.drawString(""+Math.round(score*100f)/100f, W-100, 45);

						/* COLOR DETECTION - GOES LAST */
						pnt.x = (int) ((W / 2) + (Math
								.sin(2 * Math.PI / 3f)));
						pnt.y = (int) ((H / 2) + (Math
								.cos(2 * Math.PI / 3f))) + 60+1;
						AffineTransform.getRotateInstance(
								Math.toRadians(180 - rotoffset + playerRot), W / 2,
								H / 2).transform(pnt, pnt);
						detectionpx = screen.getRGB(pnt.x, pnt.y);

						if (detectionpx == color[(score<60)?COLOR_FORE_RED:COLOR_FORE_BLU].getRGB())
						{
							gameScreen = 2;
						}
					}
					else if (gameScreen == 2) //End
					{
						/*
						 * BACKGROUND
						 */

						for(i=0;i<6;i++)
						{
							if(i%2==0)g.setColor(color[(score<60)?COLOR_BRIGHT_RED:COLOR_BRIGHT_BLU]);
							else g.setColor(color[(score<60)?COLOR_DARK_RED:COLOR_DARK_BLU]);
							poly = setPoly(3,500,rotoffset-(i*60),0,-500);
							g.fillPolygon(poly);
							poly.reset();
						}

						/*
						 * TEXT
						 */
						g.setColor(color[COLOR_WHITE]);
						g.drawString("LOSE",W/2-120,50);
						g.drawString("SCORE: "+Math.round(score*100f)/100f, (W/2)-150, H/2-60);
						if(score > best)
						{
							g.drawString("NEW BEST!",(W/2)-150, H/2-20);
						}
						else
						{
							g.drawString("BEST: " + Math.round(best*100f)/100f, (W/2)-150, H/2-20);
						}
						g.drawString("RESET",(W/2)-150,(H/2)+70);
						if(key[UP]==1)
						{
							if(score>best)best=score;
							score=0;
							rotoffset = 0f;
							playerRot = 0f;
							em = 0;
							eNum=0;
							st = new long[7]; //Start positions of enemies
							bt = new byte[st.length][]; //Enemy pattern

							for(i=0;i<st.length;i++)
							{
								switch(ra.nextInt(10))
								{
									case 0: bt[i] = p1; break;
									case 1: bt[i] = p2; break;
									case 2: bt[i] = p3; break;
									case 3: bt[i] = p4; break;
									case 4: bt[i] = p5; break;
									case 5: bt[i] = p6; break;
									case 6: bt[i] = p7; break;
									case 7: bt[i] = p8; break;
									case 8: bt[i] = p9h; break;
									case 9: bt[i] = p10h; break;
								}
								st[i]+=bt[i].length+5;
								j=i;
								while(++j<st.length)st[j]=st[i];
							}


							gameScreen=1;
						}
					}

					appletGraphics.drawImage(screen, 0, 0, null);
					Thread.sleep(10);
				}

			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param f Faces
	 * @param s Size
	 * @param r Rotation
	 * @param xo X offset
	 * @param yo Y Offset
	 * @return
	 */
	Polygon setPoly(int f,int s,float r,int xo,int yo)
	{
		Point pnt = new Point();
		Polygon p = new Polygon();
		for(int i=0;i<f;i++)
		{
			pnt.x = (int) ((W / 2) + (s * Math.sin(i * 2 * Math.PI/f))+xo);
			pnt.y = (int) ((H / 2) + (s * Math.cos(i * 2 * Math.PI/f))+yo);
			AffineTransform.getRotateInstance(
					Math.toRadians(-r), W / 2, H / 2)
					.transform(pnt, pnt);
			p.addPoint(pnt.x, pnt.y);
		}
		return p;
	}

	@Override
	public boolean handleEvent(final Event e)
	{
		switch (e.id)
		{
			case Event.KEY_ACTION:
				key[e.key] = 1;
				break;
			case Event.KEY_ACTION_RELEASE:
				key[e.key] = 0;
				break;
			case Event.KEY_PRESS:
				key[e.key & 0xff] = 1;
				break;
			case Event.KEY_RELEASE:
				// key released
				key[e.key & 0xff] = 0;
				break;
		}
		return false;
	}

	private static String str_colors =
			"\u0060\u1200" // COLOR_BRIGHT_RED
					+ "\u0050\u0c01" // COLOR_DARK_RED
					+ "\u00f6\u4813" // COLOR_FORE_RED
					+ "\u00ff\uffff" //COLOR_WHITE
					+ "\u0000\u0000" ///COLOR_WHITE
					+ "\u0005\u3267" // COLOR_BRIGHT_BLUE
					+ "\u0003\u2045" //COLOR_DARK_BLU
					+ "\u002A\u73C1"; // COLOR_FORE_BLU
}
