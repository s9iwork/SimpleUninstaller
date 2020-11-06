package work.shinjiezumi.simpleuninstaller;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity{
    /**
     * パッケージマネージャー
     */
    private PackageManager packageManager = null;

    /**
     * テキストボックス
     */
    private EditText editText = null;

    /**
     * フィールドの内容
     */
    private ListView listView = null;

    /**
     * 表示用のリストデータ
     */
    private List<CustomData> appList = null;

    /**
     * リストボックスとデータを結びつけるAdapter
     */
    private CustomAdapter adapter = null;

    /**
     * 実処理用のAppInfoのリスト
     */
    private List<ApplicationInfo> appInfoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // View初期化
        initializeViews();

        // List更新
        updateAppList();
    }

    /**
     * View初期化
     */
    private void initializeViews() {
        // ViewIdより生成
        editText = (EditText) findViewById(R.id.editText);
        listView = (ListView) findViewById(R.id.listView);
        appList = new ArrayList<CustomData>();
        packageManager = getPackageManager();

        // テキストボックス入力時のListener登録
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //
            }

            /**
             * テキスト編集後の処理
             *
             * @param s xxx
             */
            @Override
            public void afterTextChanged(Editable s) {
                boolean unfixed = false;

                // 半角英数以外であれば入力完了まで更新しない
                if (!s.toString().matches("[0-9a-zA-Z]+")) {
                    //
                    Object[] spanned = s.getSpans(0, s.length(), Object.class);
                    if (spanned != null) {
                        for (Object obj : spanned) {
                            if ((s.getSpanFlags(obj) & Spanned.SPAN_COMPOSING) == Spanned.SPAN_COMPOSING) {
                                unfixed = true;
                            }
                        }
                    }
                }
                if (!unfixed) {
                    updateAppList();
                }
            }
        });
        // テキストボックスの最大行数の設定(改行無効化)
        editText.setMaxLines(1);
        // テキストボックスの最大行数の設定
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        // リスト選択のListener登録
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ApplicationInfo ai = appInfoList.get(position);
                Uri uri = Uri.fromParts("package", ai.packageName, null);
                Intent intent = new Intent(Intent.ACTION_DELETE, uri);
                startActivity(intent);
                // startActivityForResult(intent, REQUEST_CODE_UNINSTALL);
            }
        });

        // リストボックスとデータの関連付け
        adapter = new CustomAdapter(this, 0, appList);
        listView.setAdapter(adapter);

    }

    private void updateAppList() {
        // リストデータの消去
        appList.clear();
        appInfoList.clear();

        // インストール済みのアプリケーション一覧の取得
        List<ApplicationInfo> applicationInfoList = packageManager
                .getInstalledApplications(PackageManager.GET_META_DATA);
        CustomData itemData = null;
        for (ApplicationInfo info : applicationInfoList) {
            // プリインは追加しない
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                continue;
            }
            // 自身は追加しない
            String appLabel = packageManager.getApplicationLabel(info).toString();
            if (appLabel.equals(packageManager.getApplicationLabel(this.getApplicationInfo()))) {
                continue;
            }

            // フィルタリングされるものは追加しない
            if (isFiltterdItem(info)) {
                continue;
            }

            itemData = new CustomData();
            // アイコン設定
            itemData.setImagaData(packageManager.getApplicationIcon(info));
            // リスト設定
            itemData.setTextData((String) packageManager.getApplicationLabel(info));

            // Listへ追加
            appList.add(itemData);
            appInfoList.add(info);
        }
        // Adapterへ通知
        adapter.notifyDataSetChanged();
    }

    /**
     * フィルタリング対象か非対象かを確認する。
     *
     * @param info アプリケーション情報
     * @return　フィルタリング結果(true：対象、false：非対象)
     */
    private boolean isFiltterdItem(ApplicationInfo info) {
        boolean ret = false;

        if (!editText.getText().toString().equals("")) {
            String regex = editText.getText().toString();
            Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

            Matcher m = p.matcher(info.loadLabel(packageManager).toString());

            if (!m.find()) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * レジューム時の処理
     */
    @Override
    protected void onResume() {
        super.onResume();
        // リストを更新
        updateAppList();
    }
}
