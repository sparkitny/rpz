package de.parkitny.fit.myfit.app.ui.weight;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.codetroopers.betterpickers.numberpicker.NumberPickerBuilder;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.gregacucnik.EditTextView;
import com.tramsun.libs.prefcompat.Pref;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.parkitny.fit.myfit.app.R;
import de.parkitny.fit.myfit.app.RpzApplication;
import de.parkitny.fit.myfit.app.dao.WeightEntryDao;
import de.parkitny.fit.myfit.app.entities.WeightEntry;
import de.parkitny.fit.myfit.app.ui.common.Bar;
import de.parkitny.fit.myfit.app.ui.common.Utils;
import de.parkitny.fit.myfit.app.utils.FitParams;

/**
 * A simple {@link Fragment} subclass.
 */
@Bar(Position = 3)
public class WeightFragment extends Fragment implements OnChartValueSelectedListener, View.OnClickListener {

    private static final String TAG = "WeightFragment";

    @BindView(R.id.weight_line_chart)
    protected LineChart lineChart;

    @BindView(R.id.button_delete_weight)
    protected ImageView deleteWeight;

    @BindView(R.id.current_weight)
    protected EditTextView weightText;

    @BindView(R.id.current_bmi)
    protected EditTextView bmiText;

    @BindView(R.id.user_size)
    protected EditTextView userSizeView;

    /**
     * The current {@link WeightEntry} or null if none selected
     */
    private WeightEntry currentWeightEntry;

    private WeightEntryDao weightEntryDao;

    public WeightFragment() {

        weightEntryDao = RpzApplication.DB.weightEntryDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_weight, container, false);

        ButterKnife.bind(this, view);

        lineChart.setOnChartValueSelectedListener(this);

        deleteWeight.setOnClickListener(this);

        userSizeView.setEditTextViewListener(new EditTextView.EditTextViewListener() {
            @Override
            public void onEditTextViewEditModeStart() {

            }

            @Override
            public void onEditTextViewEditModeFinish(String text) {
                storeHeight(text);
            }
        });

        userSizeView.setText(String.valueOf(Pref.getDouble(FitParams.UserSize, 0.0)));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Utils.broadcastTitle(getString(R.string.weight), getString(R.string.track_your_progress));
        fillChart();
        lineChart.animateY(1000);
        calculateBmi();
        onNothingSelected();
    }

    private void configChart() {

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLimitLinesBehindData(true);
        xAxis.setTextColor(Utils.getThemedColor(getActivity(), R.attr.myAccent));
        xAxis.setLabelRotationAngle(-90.0f);
        xAxis.setAxisMinimum(0.0f);
        xAxis.setTextSize(6.0f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0.0f);
        yAxis.setTextSize(10.0f);
        yAxis.setTextColor(R.color.md_grey_200);


        lineChart.setVisibleXRangeMinimum(5);
        lineChart.setTouchEnabled(true);
        lineChart.setScaleXEnabled(true);
        lineChart.setScaleYEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setEnabled(true);
        lineChart.getLegend().setEnabled(false);
        lineChart.setBackgroundColor(Utils.getThemedColor(getActivity(), android.R.attr.colorBackground));
        lineChart.setDescription(null);
    }

    public void fillChart() {

        configChart();

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        List<WeightEntry> weightEntries = weightEntryDao.getAll();

        if (weightEntries.size() == 0) return;

        for (WeightEntry weightEntry : weightEntries) {

            Entry entry = new Entry(entries.size(), (float) weightEntry.weight, weightEntry);
            entries.add(entry);
            labels.add(Utils.getDateString(weightEntry.weightTime, getActivity()));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, getString(R.string.weight));
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setColor(Utils.getColor(getActivity(), Utils.getAttributeId(getActivity(), R.attr.myAccent)));
        lineDataSet.setValueTextColor(Utils.getThemedColor(getActivity(), R.attr.myPrim));
        lineDataSet.setValueTextSize(10.0f);
        lineDataSet.setDrawValues(true);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSet.setColors(ColorTemplate.PASTEL_COLORS);
        lineDataSet.setLineWidth(4.0f);

        LineData data = new LineData(lineDataSet);
        data.setValueTextColor(Utils.getAttributeId(getActivity(), R.attr.myTextColor));
        data.setHighlightEnabled(true);

        lineChart.clear();
        lineChart.setData(data);

        lineChart.invalidate();
    }

    private void storeHeight(String heightText) {

        try {
            double sizeValue = Double.parseDouble(heightText);

            Pref.putDouble(FitParams.UserSize, sizeValue);
        } catch (Exception exn) {

        }
    }

    private void calculateBmi() {

        double weight = 0.0;

        if (currentWeightEntry == null) {

            WeightEntry latestWeightEntry = weightEntryDao.getLatest();

            if (latestWeightEntry != null) {
                weight = latestWeightEntry.weight;
            } else {
                bmiText.setText(getString(R.string.no_weight_value));
                return;
            }
        } else {
            weight = currentWeightEntry.weight;
        }

        String bmi = Utils.calculateBmi(weight, Utils.getUserSize());

        bmiText.setText(bmi);
    }

    private void setDeleteable(boolean deleteable) {

        if (deleteable) {
            deleteWeight.setVisibility(View.VISIBLE);
        } else {
            deleteWeight.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.button_add_weight)
    public void addWeight() {

        NumberPickerBuilder npb = new NumberPickerBuilder()
                .setFragmentManager(getActivity().getSupportFragmentManager())
                .setStyleResId(R.style.BetterPickersDialogFragment)
                .setLabelText(getString(R.string.weight_unit_kg))
                .addNumberPickerDialogHandler((reference, number, decimal, isNegative, fullNumber) -> {

                    WeightEntry weightEntry = new WeightEntry();
                    weightEntry.weight = fullNumber.doubleValue();
                    weightEntry.weightTime = System.currentTimeMillis();
                    weightEntryDao.insert(weightEntry);

                    fillChart();
                    onNothingSelected();
                    calculateBmi();
                });

        npb.show();


    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

        setDeleteable(true);

        currentWeightEntry = (WeightEntry) e.getData();

//        Log.d(TAG, String.format("Selected: %s with %s",
//                Utils.getDateTimeString(currentWeightEntry.weightTime, getActivity()),
//                currentWeightEntry.weight));

        weightText.setText(String.format("%s", String.format("%.2f", currentWeightEntry.weight)));
        calculateBmi();
    }

    @Override
    public void onNothingSelected() {

        setDeleteable(false);

        currentWeightEntry = null;

        WeightEntry latestWeightEntry = weightEntryDao.getLatest();

        if (latestWeightEntry == null) {
            weightText.setText(getString(R.string.no_weight_value));

        } else {
            weightText.setText(String.format("%s", String.format("%.2f", latestWeightEntry.weight)));
        }

        calculateBmi();
    }

    @Override
    public void onClick(View v) {

        if (currentWeightEntry != null) {
            weightEntryDao.delete(currentWeightEntry);
            currentWeightEntry = null;
            fillChart();
            onNothingSelected();
        }
    }
}
