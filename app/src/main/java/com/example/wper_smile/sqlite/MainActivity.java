package com.example.wper_smile.sqlite;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    WordsDBHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //为ListView注册上下文菜单
        ListView list = (ListView) findViewById(R.id.wordList);
        registerForContextMenu(list);

        //创建SQLiteOpenHelper对象，注意第一次运行时，此时数据库并没有被创建
        mDbHelper = new WordsDBHelper(this);

        //在列表显示全部单词
        ArrayList<Map<String, String>> items = getAll();
        setWordsListView(items);
    }

    protected void onDestroy() {
        super.onDestroy();
        mDbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                //查找
                SearchDialog();
                return true;
            case R.id.action_insert:
                //新增单词
                InsertDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.wordslistview, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        TextView textId = null;
        TextView textWord = null;
        TextView textMeaning = null;
        TextView textSample = null;

        AdapterView.AdapterContextMenuInfo info = null;
        View itemView = null;

        switch (item.getItemId()) {
            case R.id.action_delete:
                //删除单词
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                itemView = info.targetView;
                textId = (TextView) itemView.findViewById(R.id.text);
                if (textId != null) {
                    String strId = textId.getText().toString();
                    DeleteDialog(strId);
                }
                break;
            case R.id.action_update:
                //修改单词
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                itemView = info.targetView;
                textId = (TextView) itemView.findViewById(R.id.text);
                textWord = (TextView) itemView.findViewById(R.id.WordView);
                textMeaning = (TextView) itemView.findViewById(R.id.MeaningView);
                textSample = (TextView) itemView.findViewById(R.id.SampleView);
                if (textId != null && textWord != null && textMeaning != null && textSample != null) {
                    String strId = textId.getText().toString();
                    String strWord = textWord.getText().toString();
                    String strMeaning = textMeaning.getText().toString();
                    String strSample = textSample.getText().toString();
                    UpdateDialog(strId, strWord, strMeaning, strSample);
                }
                break;
        }
        return true;
    }
    //设置适配器，在列表中显示单词
    private void setWordsListView(ArrayList<Map<String, String>> items) {
        SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.item,
                new String[]{Words.Word._ID, Words.Word.Column_name_word, Words.Word.Column_name_meaning, Words.Word.Column_name_sample},
                new int[]{R.id.text, R.id.WordView, R.id.MeaningView, R.id.SampleView});

        ListView list = (ListView) findViewById(R.id.wordList);
        list.setAdapter(adapter);
    }

    private ArrayList<Map<String, String>> getAll() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                Words.Word._ID,
                Words.Word.Column_name_word,
                Words.Word.Column_name_meaning,
                Words.Word.Column_name_sample
        };

        //排序
        String sortOrder =
                Words.Word.Column_name_word + " DESC";

        Cursor c = db.query(
                Words.Word.Table_name,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        return ConvertCursor2List(c);
    }

    private ArrayList<Map<String, String>> ConvertCursor2List(Cursor cursor) {
        ArrayList<Map<String, String>> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            Map<String, String> map = new HashMap<>();
            map.put(Words.Word._ID, String.valueOf(cursor.getInt(0)));
            map.put(Words.Word.Column_name_word, cursor.getString(1));
            map.put(Words.Word.Column_name_meaning, cursor.getString(2));
            map.put(Words.Word.Column_name_sample, cursor.getString(3));
            result.add(map);
        }
        return result;
    }

    //使用insert方法增加单词
    private void Insert(String strWord, String strMeaning, String strSample) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Words.Word.Column_name_word, strWord);
        values.put(Words.Word.Column_name_meaning, strMeaning);
        values.put(Words.Word.Column_name_sample, strSample);

        long newRowId;
        newRowId = db.insert(
                Words.Word.Table_name,
                null,
                values);
    }

    //新增对话框
    private void InsertDialog() {
        final RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.insert, null);
        new AlertDialog.Builder(this)
                .setTitle("新增单词")//标题
                .setView(relativeLayout)//设置视图
                //确定按钮及其动作
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strWord = ((EditText) relativeLayout.findViewById(R.id.edit_word)).getText().toString();
                        String strMeaning = ((EditText) relativeLayout.findViewById(R.id.Meaning)).getText().toString();
                        String strSample = ((EditText) relativeLayout.findViewById(R.id.Sample)).getText().toString();

                        Insert(strWord, strMeaning, strSample);

                        ArrayList<Map<String, String>> items = getAll();
                        setWordsListView(items);

                    }
                })
                //取消按钮及其动作
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()//创建对话框
                .show();//显示对话框
    }

    //使用Sql语句删除单词
    private void DeleteUseSql(String strId) {
        String sql = "delete from words where _id='" + strId + "'";

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        db.execSQL(sql);
    }

    //删除对话框
    private void DeleteDialog(final String strId) {
        new AlertDialog.Builder(this).setTitle("删除单词").setMessage("是否真的删除单词?").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DeleteUseSql(strId);
                setWordsListView(getAll());
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).create().show();
    }

    //使用Sql语句更新单词
    private void UpdateUseSql(String strId, String strWord, String strMeaning, String strSample) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String sql = "update words set word=?,meaning=?,sample=? where _id=?";
        db.execSQL(sql, new String[]{strWord, strMeaning, strSample, strId});
    }


    //修改对话框
    private void UpdateDialog(final String strId, final String strWord, final String strMeaning, final String strSample) {
        final RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.insert, null);
        ((EditText) relativeLayout.findViewById(R.id.edit_word)).setText(strWord);
        ((EditText) relativeLayout.findViewById(R.id.Meaning)).setText(strMeaning);
        ((EditText) relativeLayout.findViewById(R.id.Sample)).setText(strSample);
        new AlertDialog.Builder(this)
                .setTitle("修改单词")//标题
                .setView(relativeLayout)//设置视图
                //确定按钮及其动作
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strNewWord = ((EditText) relativeLayout.findViewById(R.id.edit_word)).getText().toString();
                        String strNewMeaning = ((EditText) relativeLayout.findViewById(R.id.Meaning)).getText().toString();
                        String strNewSample = ((EditText) relativeLayout.findViewById(R.id.Sample)).getText().toString();

                        //使用Sql语句更新
                        UpdateUseSql(strId, strNewWord, strNewMeaning, strNewSample);
                        setWordsListView(getAll());
                    }
                })
                //取消按钮及其动作
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()//创建对话框
                .show();//显示对话框
    }

    //使用query方法查找
    private ArrayList<Map<String, String>> Search(String strWordSearch) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                Words.Word._ID,
                Words.Word.Column_name_word,
                Words.Word.Column_name_meaning,
                Words.Word.Column_name_sample
        };

        String sortOrder =
                Words.Word.Column_name_word + " DESC";

        String selection = Words.Word.Column_name_word + " LIKE ?";
        String[] selectionArgs = {"%" + strWordSearch + "%"};

        Cursor c = db.query(
                Words.Word.Table_name,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        return ConvertCursor2List(c);
    }

    //查找对话框
    private void SearchDialog() {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.searchterm, null);
        new AlertDialog.Builder(this)
                .setTitle("查找单词")//标题
                .setView(tableLayout)//设置视图
                //确定按钮及其动作
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String txtSearchWord = ((EditText) tableLayout.findViewById(R.id.search_word)).
                                getText().toString();

                        ArrayList<Map<String, String>> items = Search(txtSearchWord);

                        if (items.size() > 0) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("result", items);
                            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        } else
                            Toast.makeText(MainActivity.this, "没有找到", Toast.LENGTH_LONG).show();

                    }
                })
                //取消按钮及其动作
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()//创建对话框
                .show();//显示对话框
    }

}
