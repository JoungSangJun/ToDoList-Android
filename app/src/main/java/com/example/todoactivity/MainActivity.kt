package com.example.todoactivity

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ContentView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.Resource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    lateinit var todoRecyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //write 버튼 누르면 글쓰기 창으로 화면이동
        findViewById<ImageView>(R.id.write).setOnClickListener {
            startActivity(Intent(this, TodoWriteActivity::class.java))
        }
        todoRecyclerView = findViewById(R.id.todo_list)

        getToDoList()

        //검색창에 검색어 쓰면 조건에 맞는 글 RecyclerView에 그려줌
        findViewById<EditText>(R.id.search_edit_text).doAfterTextChanged {
            searchTodoList(it.toString())
        }
    }

    fun searchTodoList(keyword: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://mellowcode.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val retrofitService = retrofit.create(RetrofitService::class.java)
        val header = HashMap<String, String>()
        val sp = this.getSharedPreferences(
            "user_info",
            Context.MODE_PRIVATE
        )
        val token = sp.getString("token", "")
        header["Authorization"] = "token " + token!!

        retrofitService.searchTodoList(header, keyword).enqueue(object : Callback<ArrayList<ToDo>> {
            override fun onResponse(
                call: Call<ArrayList<ToDo>>,
                response: Response<ArrayList<ToDo>>
            ) {
                if (response.isSuccessful) {
                    val todoList = response.body()
                    makeToDoList(todoList!!)
                }
            }

            override fun onFailure(call: Call<ArrayList<ToDo>>, t: Throwable) {
            }

        })
    }


    //recyclerView 그려주기
    fun makeToDoList(todoList: ArrayList<ToDo>) {
        todoRecyclerView.adapter =
            ToDoListRecyclerAdapter(
                todoList!!,
                LayoutInflater.from(this@MainActivity),
                this@MainActivity
            )
    }

    //라디오박스 체크 변경 메소드
    fun changeToDoComplete(todoId: Int, activity: MainActivity) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://mellowcode.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val retrofitService = retrofit.create(RetrofitService::class.java)

        val header = HashMap<String, String>()
        val sp = this.getSharedPreferences(
            "user_info",
            Context.MODE_PRIVATE
        )
        val token = sp.getString("token", "")
        header["Authorization"] = "token " + token!!

        //이미지(라디오버튼)클릭시 할 일 체크 true, false 변경
        retrofitService.changeToDoComplete(header, todoId).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                activity.getToDoList()
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                activity.getToDoList()
            }

        })
    }

    //서버에서 나의 할 일 받아오기
    fun getToDoList() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://mellowcode.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val retrofitService = retrofit.create(RetrofitService::class.java)

        val header = HashMap<String, String>()
        val sp = this.getSharedPreferences(
            "user_info",
            Context.MODE_PRIVATE
        )
        val token = sp.getString("token", "")
        header["Authorization"] = "token " + token!!

        retrofitService.getToDoList(header).enqueue(object : Callback<ArrayList<ToDo>> {
            override fun onResponse(
                call: Call<ArrayList<ToDo>>,
                response: Response<ArrayList<ToDo>>
            ) {
                if (response.isSuccessful) {
                    val todoList = response.body()
                    makeToDoList(todoList!!)
                }
            }

            override fun onFailure(call: Call<ArrayList<ToDo>>, t: Throwable) {
                Log.d("todoo", t.message.toString())
            }

        })
    }
}

//RecyclerView Adapter
class ToDoListRecyclerAdapter(
    val todoList: ArrayList<ToDo>,
    val inflater: LayoutInflater,
    val activity: MainActivity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var previousDate: String = ""

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView

        init {
            dateTextView = itemView.findViewById(R.id.date)
        }
    }

    inner class ContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context: TextView
        val isComplete: ImageView


        init {
            context = itemView.findViewById(R.id.content)
            isComplete = itemView.findViewById(R.id.is_complete)
            isComplete.setOnClickListener {
                activity.changeToDoComplete(todoList[adapterPosition].id, activity)
            }
        }
    }

    //ViewHolder 선택에 사용(날짜 기준)
    override fun getItemViewType(position: Int): Int {
        val todo = todoList.get(position)
        val tempDate = todo.created.split("T")

        if (previousDate == tempDate[0]) {
            return 0
        } else {
            previousDate = tempDate[0]
            return 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            1 -> return DateViewHolder(inflater.inflate(R.layout.todo_date, parent, false))
            else -> return ContentViewHolder(inflater.inflate(R.layout.todo_content, parent, false))
        }
    }


    override fun getItemCount(): Int {
        return todoList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val todo = todoList.get(position)
        //is는 타입 비교
        if (holder is DateViewHolder) {
            (holder as DateViewHolder).dateTextView.text = todo.created.split("T")[0]
        } else {
            (holder as ContentViewHolder).context.text = todo.content
            // 서버에서 받아온 is_complete가 true이면 체크이미지 아니면 체크가 안 된 이미지 출력
            if (todo.is_complete) {
                (holder as ContentViewHolder).isComplete.setImageDrawable(
                    activity.resources.getDrawable(
                        R.drawable.btn_radio_check,
                        activity.theme
                    )
                )
            } else
                (holder as ContentViewHolder).isComplete.setImageDrawable(
                    activity.resources.getDrawable(
                        R.drawable.btn_radio,
                        activity.theme
                    )
                )
        }
    }
}