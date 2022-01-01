package org.icpc.tools.presentation.contest.internal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;

import com.google.common.graph.Graph;
import org.icpc.tools.contest.model.ContestUtil;
import org.icpc.tools.contest.model.IContest;
import org.icpc.tools.contest.model.IResult;
import org.icpc.tools.contest.model.ISubmission;
import org.icpc.tools.contest.model.Status;
import org.icpc.tools.presentation.contest.internal.nls.Messages;
import org.icpc.tools.presentation.contest.internal.scoreboard.AbstractScoreboardPresentation;

/**
 * Utility class from drawing shaded rectangles.
 */
public class ShadedRectangle {
	private static final int ARC = 5;
	private static final Color BG = new Color(242, 242, 242);
	// private static final Color BG = new Color(54, 54, 54, 175);
	private static final Color BG_TEXT = new Color(170, 170, 170);

	private static HashMap<Integer, Image> map = new HashMap<>();

	static class MyGradientPaint extends GradientPaint {
		private final int hash;

		public MyGradientPaint(int hash, float x1, float y1, Color color1, float x2, float y2, Color color2) {
			super(x1, y1, color1, x2, y2, color2);
			this.hash = hash;
		}

		@Override
		public int hashCode() {
			return hash;
		}
	}

	private static Paint getPaint(int h, Status status, boolean recent, boolean fts, long time) {
		Color c = null;
		int i = -1;

		int k = 0;
		if (recent) {
			// flash more than once per second
			k = (int) ((time * 45.0 / 1000.0) % (ICPCColors.CCOUNT * 2));
			if (k > (ICPCColors.CCOUNT - 1))
				k = (ICPCColors.CCOUNT * 2 - 1) - k;
		} else
			k = ICPCColors.CCOUNT / 3;
		if (status == Status.SUBMITTED) {
			i = 10 + k;
			c = ICPCColors.PENDING[0];
			// c = ICPCColors.PENDING_COLOR;
		} else if (status == Status.SOLVED) {
			if (fts) {
				i = 110 + k;
				c = new Color(173, 238, 163);
			} else {
				i = 110 + k;
				c = ICPCColors.SOLVED[0];
			}
		} else if (status == Status.FAILED) {
			i = 140 + k;
			c = ICPCColors.FAILED[0];
		}

		if (c == null || !recent)
			return c;

		return new MyGradientPaint(i, 0, 0, c, 0, h, Utility.darker(c, 0.0f));
	}

	private static Color getBorderPaint(int h, Status status, boolean recent, boolean fts, long time) {
		Color c = null;

		if (status == Status.SUBMITTED) {
			c = new Color(222, 189, 71);
		} else if (status == Status.SOLVED) {
			if (fts) {
				c = new Color(78, 136, 70);
			} else {
				c = new Color(117, 188, 107);
			}
		} else if (status == Status.FAILED) {
			c = new Color(203, 82, 56);
		}
		return c;
	}

	public static void drawRoundRect(Graphics2D g, int x, int y, int w, int h, IContest contest, IResult result,
			long time, String s) {
		Paint paint = getPaint(h, result.getStatus(), ContestUtil.isRecent(contest, result), result.isFirstToSolve(),
				time);
		Color outline = getBorderPaint(h, result.getStatus(), ContestUtil.isRecent(contest, result), result.isFirstToSolve(),
				time);
//		if (result.isFirstToSolve())
//			outline = ICPCColors.SOLVED_COLOR;

		drawRoundRect(g, x, y, w, h, paint, outline, s);
	}

	public static void drawRoundRect(Graphics2D g, int x, int y, int w, int h, IContest contest, Status status,
			int contestTime, long time, String s) {
		Paint paint = getPaint(h, status, ContestUtil.isRecent(contest, contestTime), false, time);
		drawRoundRect(g, x, y, w, h, paint, null, s);
	}

	public static void drawRoundRect(Graphics2D g, int x, int y, int w, int h, IContest contest, ISubmission submission,
			long time, String s) {
		boolean isFTS = contest.isFirstToSolve(submission);
		Paint paint = getPaint(h, contest.getStatus(submission), ContestUtil.isRecent(contest, submission), isFTS, time);
		Color outline = null;
		if (isFTS)
			outline = ICPCColors.SOLVED_COLOR;

		drawRoundRect(g, x, y, w, h, paint, outline, s);
	}

	public static void drawRoundRect(Graphics2D g, int x, int y, int w, int h, Paint paint) {
		drawRoundRect(g, x, y, w, h, paint, null, null);
	}

	public static void drawRoundRect(Graphics2D g, int x, int y, int w, int h, Paint paint, Color outline) {
		drawRoundRect(g, x, y, w, h, paint, outline, null);
	}

	public static void drawRoundRectPlain(Graphics2D g, int x, int y, int w, int h, String s) {
		drawRoundRect(g, x, y, w, h, null, new Color(170, 170, 170), s);
	}

	private static int getKey(int w, int h, Paint paint, Color outline, String s) {
		int key = w + 31 * h;
		if (s != null)
			key = key + 31 * 31 * s.hashCode();
		if (paint != null)
			key = key * 31 + paint.hashCode();
		if (outline != null)
			key = key * 31 + outline.hashCode();
		return key;
	}

	public static void drawRoundRect(Graphics2D g, int x, int y, int w, int h, Paint paint, Color outline, String s) {
		int key = getKey(w, h, paint, outline, s);
		Image image = map.get(key);
		if (image != null) {
			g.scale(1.0/2, 1.0/2);
			g.drawImage(image, x*2, y*2, null);
			g.scale(1.0*2, 1.0*2);
			return;
		}
		image = createRoundRect(g, x, y, w, h, paint, outline, s);
		map.put(key, image);
		g.scale(1.0/2, 1.0/2);
		g.drawImage(image, x*2, y*2, null);
		g.scale(1.0*2, 1.0*2);
	}

	private static Image createRoundRect(Graphics2D g2, int x, int y, int ww, int hh, Paint paint, Color outline, String s) {
		GraphicsConfiguration gfx_config = GraphicsEnvironment.
				getLocalGraphicsEnvironment().getDefaultScreenDevice().
				getDefaultConfiguration();
//
//		ww *= 4;
//		hh *= 4;
		BufferedImage new_image = gfx_config.createCompatibleImage(ww*2, hh*2, Transparency.TRANSLUCENT);

		Graphics2D g = (Graphics2D) new_image.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
//		g.translate(x, y);
		g.scale(2, 2);

		int h = hh;
		int w = ww;
		if (outline != null) {
			g.translate(1, 1);
			w -= 2;
			h -= 2;
		}

		GeneralPath gp = new GeneralPath();
		gp.moveTo(ARC, 0);
		gp.lineTo(w - ARC, 0);
		gp.quadTo(w, 0, w, ARC);
		gp.lineTo(w, h - ARC);
		gp.quadTo(w, h, w - ARC, h);
		gp.lineTo(ARC, h);
		gp.quadTo(0, h, 0, h - ARC);
		gp.lineTo(0, ARC);
		gp.quadTo(0, 0, ARC, 0);
		gp.closePath();

		if (paint == null)
			g.setPaint(BG);
		else
			g.setPaint(paint);
		g.fill(gp);

		if (outline != null) {
			g.setColor(outline);
			g.setStroke(new BasicStroke(1));
			g.draw(gp);
			g.setStroke(new BasicStroke(1));
		}

		if (s != null) {
			if (paint == null) {
				g.setFont(AbstractScoreboardPresentation.problemFont);
				FontMetrics fm = g.getFontMetrics();

				g.setColor(BG_TEXT);
				g.drawString(s, (w - fm.stringWidth(s)) / 2, (h + fm.getHeight()) / 2 - fm.getDescent() + 1);
			} else {
				g.setFont(AbstractScoreboardPresentation.statusFont);
				FontMetrics fm = g.getFontMetrics();

				if (paint == ICPCColors.PENDING_COLOR)
					g.setColor(new Color(54, 54, 54));
				else
					g.setColor(new Color(54, 54, 54));
				g.drawString(s, (w - fm.stringWidth(s)) / 2, h / 2 + fm.getHeight() / 2 - fm.getDescent() + 1);
			}
		}

		g.dispose();

		return new_image;
	}
}