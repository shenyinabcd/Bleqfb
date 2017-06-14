package com.bqkj.bleqfb.activity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.bqkj.bleqfb.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AchartAcitivty extends Activity{

	public String title = "历史数据";
	private XYSeries series;
	private XYMultipleSeriesDataset mDataset;
	private GraphicalView chart;
	private XYMultipleSeriesRenderer renderer;
	private double addX = -1, addY;
	private Context context;
	int[] xv = new int[100];
	int[] yv = new int[100];
	double d1 = 0.0;
	double d2 = 0.0;
	//    double t2 = 0.0;
//    double t1 = 0.0;
	public double max = 0;
	public double min = 0;
	public double sum = 0;
	public TextView tv_max, tv_min, tv_average;
	public String mfilepath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_achart);

		addChart();

		tv_max = (TextView)findViewById(R.id.tv_max);
		tv_min = (TextView)findViewById(R.id.tv_min);
		tv_average = (TextView)findViewById(R.id.tv_average);
		Intent it =getIntent();
		mfilepath = it.getStringExtra("filepath");
//			   		title = mfilepath;
		new Thread() {
			Handler hd = new Handler() {
				Intent intent =getIntent();
				String filepath = intent.getStringExtra("filepath");
				public void handleMessage(Message msg) {
					try {
						readfile(filepath);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					super.handleMessage(msg);
				}
			};
			public void run(){
				Message message = new Message();
				hd.sendMessage(message);
			}
		}.start();
	}
	/**
	 * 初始化表格，绘制表格属性
	 */
	private void addChart() {

		context = getApplicationContext();

		// 这里获得main界面上的布局，下面会把图表画在这个布局里面
		LinearLayout layout = (LinearLayout) findViewById(R.id.achart);

		// 这个类用来放置曲线上的所有点，是一个点的集合，根据这些点画出曲线
		series = new XYSeries(title);

		// 创建一个数据集的实例，这个数据集将被用来创建图表
		mDataset = new XYMultipleSeriesDataset();

		// 将点集添加到这个数据集中
		mDataset.addSeries(series);

		// 以下都是曲线的样式和属性等等的设置，renderer相当于一个用来给图表做渲染的句柄
		int color = Color.BLUE;
		PointStyle style = PointStyle.CIRCLE;
		renderer = buildRenderer(color, style, true);

		// 设置好图表的样式
		setChartSettings(renderer, mfilepath, "数量(个)", "位移(mm)", 0, 20, -30, 30,
				Color.GRAY, Color.RED);
		// renderer.setXAxisMax(max)
		// 生成图表
		chart = ChartFactory.getLineChartView(context, mDataset, renderer);

		// 将图表添加到布局中去
		layout.addView(chart, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		addX = 0;
		addY = 0;
		updateChart();
	}

	protected XYMultipleSeriesRenderer buildRenderer(int color,
													 PointStyle style, boolean fill) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

		// 设置图表中曲线本身的样式，包括颜色、点的大小以及线的粗细等
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(color);//颜色
		r.setPointStyle(style);//点类型，圆形方形三角形
		r.setFillPoints(fill);//空心
		r.setLineWidth(4);//粗细
		renderer.addSeriesRenderer(r);
		return renderer;
	}

	// 绘制图表
	protected void setChartSettings(XYMultipleSeriesRenderer renderer,
									String title, String xTitle, String yTitle, double xMin,
									double xMax, double yMin, double yMax, int axesColor,
									int labelsColor) {
		// 有关对图表的渲染可参看api文档
		renderer.setChartTitle(title);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor); // xy轴的颜色
		renderer.setShowGrid(true); // 显示网格
		renderer.setGridColor(Color.GRAY); // 网格颜色
		renderer.setApplyBackgroundColor(false); // 不使用默认背景色
		// renderer.setBackgroundColor(Color.rgb(0xD9, 0xff, 0xff)); // 设置图表背景色
		renderer.setMarginsColor(Color.WHITE); // 设置空白区颜色
		renderer.setLabelsColor(Color.BLACK); // xy轴标签名字的颜色

		renderer.setXLabels(20); // 设置合适的刻度,在轴上显示的数量是 MAX / labels
		renderer.setXLabelsColor(Color.BLACK);
		renderer.setYLabelsColor(0, Color.BLACK);

		renderer.setAxesColor(Color.BLACK);//坐标轴颜色
		renderer.setLabelsColor(Color.BLACK); // xy轴标签名字的颜色
		renderer.setYLabels(30); // 纵坐标标签个数
		renderer.setLabelsTextSize(20); // 设置刻度显示文字的大小(XY轴都会被设置)
		renderer.setAxisTitleTextSize(20); // 设置坐标轴标签字体大小
		renderer.setChartTitleTextSize(20); // 标题字体大小
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setPointSize(3f);
		renderer.setShowLegend(true); // 是否显示图例
	}
	/**
	 * 更新表格数据
	 */
	private void updateChart() {
//		// 设置好下一个需要增加的节点
//		addX = 0;
//		addY = (int) (Math.random() * 90);
//		// 移除数据集中旧的点集
		mDataset.removeSeries(series);

		// 判断当前点集中到底有多少点，因为屏幕总共只能容纳100个，所以当点数超过100时，长度永远是100
		int length = series.getItemCount();
		if (length > 100) {
			length = 100;
		}
		// 将旧的点集中x和y的数值取出来放入backup中，并且将x的值加1，造成曲线向右平移的效果
		for (int i = 0; i < length; i++) {
			xv[i] = (int) series.getX(i) + 1;
			yv[i] = (int) series.getY(i);
		}

		for (int i = 0; i < length; i++) {
			xv[i] = (int) series.getX(i);
			yv[i] = (int) series.getY(i);
		}


		// 点集先清空，为了做成新的点集而准备
		series.clear();

		// 将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
		// 这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点

		for (int k = 0; k < length; k++) {
			series.add(xv[k], yv[k]);
		}
		series.add(addX, addY);
		// 在数据集中添加新的点集
		mDataset.addSeries(series);

		// 视图更新，没有这一步，曲线不会呈现动态
		// 如果在非UI主线程中，需要调用postInvalidate()，具体参考api
		chart.invalidate();
	}
	//读取文件记录
	public void readfile(String filePath) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(filePath)));
		int i = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {

			String[] s = line.split(";");
			d1 = Double.parseDouble(s[1].trim().replace("mm", ""));//数据
			d2 = i++;
			sum = sum + d1;
			if (d1 > max) {
				max = d1;
			}else if (d1 < min) {
				min = d1;
			}


			addX = d2;
			addY = d1;
			updateChart();
//        	d2 = Double.parseDouble(s[3]);
//        	String s1 = line.substring(5, 11);
//        	d1 = Double.parseDouble(s1);
//        	String s2 = line.substring(16,22);
//        	d2 = Double.parseDouble(s2);


//        	if(d2 != t2||d1 != t1){
//        		addX = d2;
//        		addY = d1;
//        		updateChart();
//        		t2 = d2;
//        		t1 = d1;
//        	}
		}
		tv_max.setText("最大值为：  "+max+"mm");
		tv_min.setText("最小值为：  "+min+"mm");
		tv_average.setText("平均值为：  "+sum+"÷"+(int)(d2+1)+" = "+sum/d2+"mm");

		br.close();
	}
}
