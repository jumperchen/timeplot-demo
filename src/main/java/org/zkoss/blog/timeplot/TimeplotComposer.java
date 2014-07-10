package org.zkoss.blog.timeplot;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.chart.Charts;
import org.zkoss.chart.ChartsSelectionEvent;
import org.zkoss.chart.LinearGradient;
import org.zkoss.chart.Marker;
import org.zkoss.chart.PlotBand;
import org.zkoss.chart.PlotLine;
import org.zkoss.chart.Point;
import org.zkoss.chart.Series;
import org.zkoss.chart.XAxis;
import org.zkoss.chart.plotOptions.AreaPlotOptions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Window;

public class TimeplotComposer extends SelectorComposer<Window> {

	@Wire
	Charts chart;

	@Wire
	Button btn;

	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);

		// set the xAxis type to support datetime format
		XAxis xAxis = chart.getXAxis();
		xAxis.setType("datetime");

		// styling the chart layout
		chart.getYAxis().setTitle("Value");
		chart.getYAxis().setMin(0);
		chart.getYAxis().setGridLineWidth(0);
		chart.getYAxis().setLineWidth(1);

		// specify the simple tooltip for the point format
		chart.getTooltip().setPointFormat("{point.x:%e. %b}: {point.y}");
		
		// disable the legend in this chart
		chart.getLegend().setEnabled(false);
		
		// paint the time plot chart by using the same zoom out API (the default zoom value is 1)
		doZoomOut();
	}

	@Listen("onSelection = #chart")
	public void doSelection(ChartsSelectionEvent event) {
		// doing the zoom in function
		double min = event.getXAxisMin().longValue();
		double max = event.getXAxisMax().longValue();
		updateSelection(min, max);
		
		// enable the zoom out button
		btn.setVisible(true);
	}

	@Listen("onClick = #btn")
	public void doZoomOut() {
		Double[][] data = TimeplotData.getData();
		updateSelection(data[0][0], data[data.length-1][0]);
		btn.setVisible(false);
	}

	private void updateSelection(double min, double max) {
		// reset
		XAxis xAxis = chart.getXAxis();

		if (xAxis.getPlotBands() != null) {
			for (PlotBand pb : new ArrayList<PlotBand>(xAxis.getPlotBands())) {
				xAxis.removePlotBand(pb);
			}
		}
		if (xAxis.getPlotLines() != null) {
			for (PlotLine pl : new ArrayList<PlotLine>(xAxis.getPlotLines())) {
				xAxis.removePlotLine(pl);
			}
		}

		Series series1 = chart.getSeries();
		
		// styling the color of the point
		AreaPlotOptions area = chart.getPlotOptions().getArea();
		area.setLineColor("rgba(0,139,182,1)");
		Marker marker = chart.getPlotOptions().getArea().getMarker();
		marker.setRadius(2);
		
		// styling the color of the series	
		LinearGradient seriesColor = new LinearGradient(1, 0, 1, 1);
		seriesColor.addStop(0, "rgba(0,139,182,1)");
		seriesColor.addStop(1, "rgba(255,255,255,0.5)");
		series1.setColor(seriesColor);
		
		// styling the color of the plotband	
		LinearGradient plotbandColor = new LinearGradient(1, 1, 1, 0);
		plotbandColor.addStop(0, "#008bb6");
		plotbandColor.addStop(1, "#ffffff");
		
		series1.setType("area");
		List<Point> data = series1.getData();
		if (data != null)
			data.clear();
		
		// fill in the data of New Legal Permanent Residents in the U.S
		for (Double[] val : TimeplotData.getData()) {
			if (min < val[0] && max > val[0]) {
				Point point = new Point(val[0], val[1]);
				point.setColor("blank");
				series1.addPoint(point);
			}
		}
		
		// fill in the data of the U.S. History
		for (Object[] val : TimeplotData.getEvents()) {
			if (val.length == 2) {
				
				// without the end date, we use PlotLine to display
				if (min < (Double) val[0] && max > (Double) val[0]) {
					PlotLine line = new PlotLine();
					final String title = (String) val[1];
					line.setValue((Number) val[0]);
					line.setWidth(3);
					line.setColor("rgba(0,139,182,0.5)");
					line.addEventListener("onMouseOver",
							new EventListener<MouseEvent>() {
								public void onEvent(MouseEvent event)
										throws Exception {
									Clients.showNotification(title, "info",
											event.getTarget(),
											event.getPageX() == 0 ? event.getX() : event.getPageX(),
											event.getPageY() == 0 ? event.getY() : event.getPageY()
															, 1500, false);
								}
							});
					xAxis.addPlotLine(line);
				}
			} else {
				double v0 = (Double) val[0];
				double v1 = (Double) val[1];
				if (v0 < min && v1 < max)
					v0 = min;
				if (v0 > min && v1 > max)
					v1 = max;
				if (v0 < min && v1 > max) {
					v0 = min;
					v1 = max;
				}

				// with the end date, we use PlotBand to display
				if (min <= v1 && max >= v1) {
					final PlotBand band = new PlotBand();
					band.setFrom((Number) val[0]);
					band.setTo((Number) val[1]);
					band.setColor(plotbandColor);
					final String title = (String) val[2];
					band.addEventListener("onMouseOver",
							new EventListener<MouseEvent>() {
								public void onEvent(MouseEvent event)
										throws Exception {
									Clients.showNotification(title, "info",
											event.getTarget(),
											event.getPageX() == 0 ? event.getX() : event.getPageX(),
											event.getPageY() == 0 ? event.getY() : event.getPageY(), 1500, false);
								}
							});
					xAxis.addPlotBand(band);
				}
			}
		}
	}
}
