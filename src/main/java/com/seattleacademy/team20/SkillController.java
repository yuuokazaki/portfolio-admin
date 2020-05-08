package com.seattleacademy.team20;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Handles requests for the application home page.
 */
@Controller
public class SkillController {

	private static final Logger logger = LoggerFactory.getLogger(SkillController.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@RequestMapping(value = "/skillUpload", method = RequestMethod.GET)
	public String skillUpload(Locale locale, Model model) {
		logger.info("Welcome SkillUpload! The client locale is {}.", locale);

		try {
			initialize();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		};
		List<SkillCategory> categories =  selectSkillCategories();
		List<Skill> skills = selectSkills();

		uploadSkill(categories, skills);

		return "skillUpload";
	}

	public List<SkillCategory> selectSkillCategories() {
		//skill_categoriesテーブルのデータを全件取得するSQL
		final String sql = "SELECT * FROM skill_categories";
		//SQL実行
		return jdbcTemplate.query(sql, new RowMapper<SkillCategory>() {
			public SkillCategory mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new SkillCategory(rs.getInt("id"), rs.getString("category"));
			}
		});
	}

	public List<Skill> selectSkills() {
		//skillsテーブルのデータを全件取得するSQL
		final String sql = "SELECT * FROM skills";
		//SQL実行
		return jdbcTemplate.query(sql, new RowMapper<Skill>() {
			public Skill mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new Skill(rs.getInt("id"), rs.getInt("category_id"), rs.getString("name"), rs.getInt("score"));
			}
		});
	}

	private FirebaseApp app;

	//SDK初期化
	public void initialize() throws IOException {
		FileInputStream refreshToken = new FileInputStream(
				"/Users/okazakiyu/seattle-key/dev-portfolio-24b63-firebase-adminsdk-4s21y-806424bc00.json");
		FirebaseOptions options = new FirebaseOptions.Builder()
				.setCredentials(GoogleCredentials.fromStream(refreshToken))
				.setDatabaseUrl("https://dev-portfolio-24b63.firebaseio.com/")
				.build();
		app = FirebaseApp.initializeApp(options, "other");
	}

	public void uploadSkill(List<SkillCategory> categories, List<Skill> skills) {
		final FirebaseDatabase database = FirebaseDatabase.getInstance(app);
		DatabaseReference ref = database.getReference("skillCategories");

		//整形
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		Map<String, Object> dataMap;
		for (SkillCategory category : categories) {
			dataMap = new HashMap<>();
			dataMap.put("category", category.getCategory());
			dataMap.put("skills", skills.stream()
					.filter(s -> s.getCategoryId() == category.getId())
					.collect(Collectors.toList()));
			dataList.add(dataMap);
		}

		ref.setValue(dataList, new DatabaseReference.CompletionListener() {
			@Override
			public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
				if (databaseError != null) {
					System.out.println("Data could be saved" + databaseError.getMessage());
				} else {
					System.out.println("Data save successfuly");
				}
			}
		});
	}
}