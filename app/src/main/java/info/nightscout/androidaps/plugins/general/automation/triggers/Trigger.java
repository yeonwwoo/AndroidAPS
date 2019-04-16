package info.nightscout.androidaps.plugins.general.automation.triggers;

import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Optional;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import info.nightscout.androidaps.MainApp;
import info.nightscout.androidaps.R;

public abstract class Trigger {

    public enum Comparator {
        IS_LESSER,
        IS_EQUAL_OR_LESSER,
        IS_EQUAL,
        IS_EQUAL_OR_GREATER,
        IS_GREATER,
        IS_NOT_AVAILABLE;

        public @StringRes int getStringRes() {
            switch (this) {
                case IS_LESSER:
                    return R.string.islesser;
                case IS_EQUAL_OR_LESSER:
                    return R.string.isequalorlesser;
                case IS_EQUAL:
                    return R.string.isequal;
                case IS_EQUAL_OR_GREATER:
                    return R.string.isequalorgreater;
                case IS_GREATER:
                    return R.string.isgreater;
                case IS_NOT_AVAILABLE:
                    return R.string.isnotavailable;
                default:
                    return R.string.unknown;
            }
        }

        public <T extends Comparable> boolean check(T obj1, T obj2) {
            if (obj1 == null || obj2 == null)
                return this.equals(Comparator.IS_NOT_AVAILABLE);

            int comparison = obj1.compareTo(obj2);
            switch (this) {
                case IS_LESSER:
                    return comparison < 0;
                case IS_EQUAL_OR_LESSER:
                    return comparison <= 0;
                case IS_EQUAL:
                    return comparison == 0;
                case IS_EQUAL_OR_GREATER:
                    return comparison >= 0;
                case IS_GREATER:
                    return comparison > 0;
                default:
                    return false;
            }
        }

        public static List<String> labels() {
            List<String> list = new ArrayList<>();
            for(Comparator c : values()) {
                list.add(MainApp.gs(c.getStringRes()));
            }
            return list;
        }
    }

    protected TriggerConnector connector = null;

    Trigger() {
    }

    public TriggerConnector getConnector() {
        return connector;
    }

    public abstract boolean shouldRun();


    public abstract String toJSON();

    /*package*/ abstract Trigger fromJSON(String data);

    public abstract int friendlyName();

    public abstract String friendlyDescription();

    public abstract Optional<Integer> icon();

    public abstract void executed(long time);

    public abstract Trigger duplicate();

    public static Trigger instantiate(String json) {
        try {
            return instantiate(new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Trigger instantiate(JSONObject object) {
        try {
            String type = object.getString("type");
            JSONObject data = object.getJSONObject("data");
            Class clazz = Class.forName(type);
            return ((Trigger) clazz.newInstance()).fromJSON(data.toString());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public View createView(Context context, FragmentManager fragmentManager) {
        final int padding = MainApp.dpToPx(4);

        LinearLayout root = new LinearLayout(context);
        root.setPadding(padding, padding, padding, padding);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView title = new TextView(context);
        title.setText(friendlyName());
        root.addView(title);

        return root;
    }
}