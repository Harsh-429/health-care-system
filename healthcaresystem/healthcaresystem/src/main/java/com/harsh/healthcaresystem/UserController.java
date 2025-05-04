package com.harsh.healthcaresystem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/getProfile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Fetching profile for email: " + email);

        try {
            // SQL Select Query
            String sql = "SELECT username, age, gender, blood_group, height_cm, weight_kg, bmi, exercise_plan, dietary_preference FROM users WHERE email = ?";

            // Execute query and retrieve user profile
            Map<String, Object> userProfile = jdbcTemplate.queryForMap(sql, email);

            return ResponseEntity.ok(Map.of("status", "success", "profile", userProfile));
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(404).body(Map.of("status", "error", "message", "User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("status", "error", "message", "An error occurred while fetching profile"));
        }
    }

    @PostMapping("/updateProfile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody UserData userData) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Email: " + email);
        try {
            // Calculate BMI
            double heightInMeters = userData.getHeightCm() / 100.0;
            double bmi = userData.getWeightKg() / (heightInMeters * heightInMeters);

            // SQL Update Query
            String sql = "UPDATE users SET age = ?, gender = ?, blood_group = ?, height_cm = ?, weight_kg = ?, bmi = ?, exercise_plan = ?, dietary_preference = ? WHERE email = ?";

            // Execute the update query
            int rowsAffected = jdbcTemplate.update(
                    sql,
                    userData.getAge(),
                    userData.getGender(),
                    userData.getBloodGroup(),
                    userData.getHeightCm(),
                    userData.getWeightKg(),
                    bmi, // Use the calculated BMI here
                    userData.getExercisePlan(),
                    userData.getDietaryPreference(),
                    email);

            // Check if any rows were updated and return response
            if (rowsAffected > 0) {
                return ResponseEntity.ok(Map.of("status", "success", "message", "Profile updated successfully"));
            } else {
                return ResponseEntity.status(404).body(Map.of("status", "error", "message", "User not found"));
            }
        } catch (Exception e) {
            // Handle exception and return an error response
            return ResponseEntity.status(500)
                    .body(Map.of("status", "error", "message", "An error occurred while updating profile"));
        }
    }

    @PostMapping("/getExercisePlan")
    public ResponseEntity<Map<String, Object>> getDiet(@RequestBody UserData userData) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            String sql = "SELECT exercise_plan FROM users WHERE email = ?";
            String exercisePlan = jdbcTemplate.queryForObject(sql, String.class, email);

            List<Map<String, Object>> workoutPlan = getWorkoutPlan(exercisePlan);

            return ResponseEntity.ok(Map.of("status", "success", "workout_routine", workoutPlan));

        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(404).body(Map.of("status", "error", "message", "User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("status", "error", "message", "An error occurred while fetching workout plan"));
        }
    }

    private List<Map<String, Object>> getWorkoutPlan(String exerciseChoice) {
        List<Map<String, Object>> activities = new ArrayList<>();

        switch (exerciseChoice) {
            case "Athlete":
                activities.add(Map.of("activity", "Sprint Intervals (6x400m)", "calories", 400));
                activities.add(Map.of("activity", "Strength Training (3 sets)", "calories", 300));
                activities.add(Map.of("activity", "Core Workout (20 min)", "calories", 150));
                activities.add(Map.of("activity", "Dynamic Stretching (15 min)", "calories", 100));
                break;

            case "Gym":
                activities.add(Map.of("activity", "Treadmill (30 min)", "calories", 250));
                activities.add(Map.of("activity", "Weight Lifting (4 sets)", "calories", 200));
                activities.add(Map.of("activity", "Elliptical (20 min)", "calories", 150));
                activities.add(Map.of("activity", "Stretching (10 min)", "calories", 50));
                break;

            case "Swimmer":
                activities.add(Map.of("activity", "Freestyle (500m)", "calories", 300));
                activities.add(Map.of("activity", "Mixed Strokes (400m)", "calories", 250));
                activities.add(Map.of("activity", "Technique Drills (15 min)", "calories", 100));
                activities.add(Map.of("activity", "Warm-up Swim (200m)", "calories", 100));
                break;

            case "Yoga":
                activities.add(Map.of("activity", "Sun Salutations (5 rounds)", "calories", 150));
                activities.add(Map.of("activity", "Standing Poses (20 min)", "calories", 100));
                activities.add(Map.of("activity", "Floor Poses (20 min)", "calories", 100));
                activities.add(Map.of("activity", "Meditation (15 min)", "calories", 50));
                break;

            default:
                // Return empty list or add a fallback plan if you're feeling fancy
                break;
        }

        return activities;
    }

    @PostMapping("/getDietPlan")
    public ResponseEntity<Map<String, Object>> getDietPlan(@RequestBody UserData userData) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            // SQL to get BMI and dietary preference
            String sql = "SELECT bmi, dietary_preference FROM users WHERE email = ?";
            Map<String, Object> userInfo = jdbcTemplate.queryForMap(sql, email);

            double bmi = ((BigDecimal) userInfo.get("bmi")).doubleValue();
            String dietPref = (String) userInfo.get("dietary_preference");

            List<String> dietPlan = getDietPlanBasedOnBmiAndDiet(bmi, dietPref);

            return ResponseEntity.ok(Map.of("status", "success", "diet_plan", dietPlan));

        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(404).body(Map.of("status", "error", "message", "User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("status", "error", "message", "An error occurred while fetching diet plan"));
        }
    }

    private List<String> getDietPlanBasedOnBmiAndDiet(double bmi, String dietPref) {
        List<String> dietPlan = new ArrayList<>();

        if (dietPref.equalsIgnoreCase("Veg")) {
            if (bmi < 18.5) { // Underweight - Bulk
                dietPlan.add("- Breakfast: Oatmeal with fruits & nuts (400 cal)");
                dietPlan.add("- Lunch: Lentil curry with rice (600 cal)");
                dietPlan.add("- Dinner: Veg stir-fry with tofu (500 cal)");
                dietPlan.add("- Snacks: Protein shake, almonds (300 cal)");
            } else if (bmi <= 24.9) { // Normal - Maintain
                dietPlan.add("- Breakfast: Avocado toast (300 cal)");
                dietPlan.add("- Lunch: Quinoa veggie salad (400 cal)");
                dietPlan.add("- Dinner: Bean curry with chapati (400 cal)");
                dietPlan.add("- Snacks: Fruit, yogurt (200 cal)");
            } else { // Overweight - Cut
                dietPlan.add("- Breakfast: Green smoothie (200 cal)");
                dietPlan.add("- Lunch: Vegetable soup (250 cal)");
                dietPlan.add("- Dinner: Grilled veggies with hummus (300 cal)");
                dietPlan.add("- Snacks: Cucumber slices (50 cal)");
            }
        } else { // Non-Veg
            if (bmi < 18.5) { // Underweight - Bulk
                dietPlan.add("- Breakfast: Eggs & toast (400 cal)");
                dietPlan.add("- Lunch: Chicken with rice (600 cal)");
                dietPlan.add("- Dinner: Fish with veggies (500 cal)");
                dietPlan.add("- Snacks: Protein shake, nuts (300 cal)");
            } else if (bmi <= 24.9) { // Normal - Maintain
                dietPlan.add("- Breakfast: Veggie omelette (300 cal)");
                dietPlan.add("- Lunch: Grilled chicken salad (400 cal)");
                dietPlan.add("- Dinner: Baked salmon with quinoa (400 cal)");
                dietPlan.add("- Snacks: Fruit, boiled egg (200 cal)");
            } else { // Overweight - Cut
                dietPlan.add("- Breakfast: Egg white scramble (200 cal)");
                dietPlan.add("- Lunch: Turkey with veggies (250 cal)");
                dietPlan.add("- Dinner: Grilled fish with salad (300 cal)");
                dietPlan.add("- Snacks: Celery sticks (50 cal)");
            }
        }

        return dietPlan;
    }

}
