package com.example.food.utils;

import android.util.Log;

import com.example.food.model.Post;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Data Seeder - Utility class for uploading test post data to Firebase
 * This is a temporary tool class used only for initializing database content
 */
public class DataSeeder {
    private static final String TAG = "DataSeeder";
    private static final String COLLECTION_POSTS = "posts";
    
    private FirebaseFirestore db;

    public DataSeeder() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Callback interface
     */
    public interface SeedCallback {
        void onSuccess(int count);
        void onError(Exception e);
    }

    /**
     * Generate and upload test post data
     */
    public void seedPosts(SeedCallback callback) {
        List<Post> posts = generateSamplePosts();
        
        int totalPosts = posts.size();
        int[] uploadedCount = {0};
        int[] failedCount = {0};
        
        for (Post post : posts) {
            db.collection(COLLECTION_POSTS)
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    uploadedCount[0]++;
                    Log.d(TAG, "Post uploaded: " + documentReference.getId() + " - " + post.getTitle());
                    
                    if (uploadedCount[0] + failedCount[0] == totalPosts) {
                        if (failedCount[0] == 0) {
                            callback.onSuccess(uploadedCount[0]);
                        } else {
                            callback.onError(new Exception("Failed to upload " + failedCount[0] + " posts"));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    failedCount[0]++;
                    Log.e(TAG, "Error uploading post: " + post.getTitle(), e);
                    
                    if (uploadedCount[0] + failedCount[0] == totalPosts) {
                        callback.onError(new Exception("Failed to upload " + failedCount[0] + " posts"));
                    }
                });
        }
    }

    /**
     * Generate sample post data
     */
    private List<Post> generateSamplePosts() {
        List<Post> posts = new ArrayList<>();
        Random random = new Random();
        
        // Food-related title and content data
        String[][] foodData = {
            {"Amazing Hot Pot Experience 🔥", "Visited an amazing hot pot restaurant today! The spicy and savory broth was incredible, and the ingredients were super fresh. Their handmade beef balls were absolutely delicious, bursting with juice. I highly recommend the dual-flavor broth so you can enjoy both spicy and mild options. Great value at around $20 per person!", "FoodLover88", "Restaurant Review"},
            {"Homemade Tiramisu Tutorial ☕", "Made tiramisu at home this weekend, and it was easier than expected! The creamy mascarpone cheese combined with rich coffee flavor just melts in your mouth. The key is to thoroughly soak the ladyfingers in coffee liqueur. Made a whole tray and everyone said it was better than store-bought!", "SweetToothChef", "Desserts"},
            {"Peking Duck Review 🦆", "Tried authentic Peking duck at a famous restaurant. The skin was crispy, the meat tender, and wrapped with scallions and sweet bean sauce in thin pancakes - absolutely heavenly! Watching the chef carve the duck tableside was a highlight. A bit pricey but definitely worth experiencing.", "FoodCritic_Mike", "Chinese Cuisine"},
            {"Japanese Sushi Making 🍣", "First day learning to make sushi! Fresh salmon with vinegared rice - simple but delicious. Making nigiri looks easy but is actually quite challenging. You need to control both the rice temperature and pressure carefully. Not perfect on my first try, but the taste was good! Will try hand rolls next time.", "CookingNewbie", "Japanese Food"},
            {"Shanghai Soup Dumplings 🥟", "These soup dumplings truly live up to their reputation! Thin skin, generous filling, and rich broth. With a gentle bite, the delicious soup bursts in your mouth. Paired with a bowl of hot wonton soup, it's the perfect breakfast! Long wait but absolutely worth it.", "FoodBloggerLisa", "Asian Street Food"},
            {"Thai Tom Yum Soup 🍜", "Recreated authentic Thai Tom Yum soup at home! Spicy and sour, with fresh shrimp, mushrooms, and lemongrass. The aroma of lemongrass and galangal was fantastic. Perfect with rice or noodles. The soul of this soup is balancing the sour and spicy flavors. So warming!", "ThaiCuisinelover", "Thai Food"},
            {"French Macaron Challenge 🎨", "First attempt at making macarons - quite challenging! Temperature and humidity need perfect control, easy to fail. Spent all day and finally succeeded with a few. The colorful result was beautiful. Taste was good but a bit too sweet, will reduce sugar next time.", "BakingMaster_Amy", "French Pastry"},
            {"Korean BBQ Experience 🥓", "Checked out a new Korean BBQ spot! The pork belly was perfectly grilled, crispy outside and tender inside, with aromatic oils. Paired with lettuce, kimchi, and various side dishes - a feast for the taste buds. They even gave us seafood soup and cold noodles. Service was excellent. Highly recommend their special sauce!", "MeatLoverJohn", "Korean BBQ"},
            {"Creative Pasta Dish 🍝", "Made garlic bacon pasta today and was surprised how easy it was to achieve restaurant quality! Cook pasta to al dente, then toss with bacon and garlic, finish with black pepper and parmesan. The rich garlic aroma and savory bacon perfectly complement each other.", "PastaEnthusiast", "Italian Cuisine"},
            {"Dim Sum Brunch 🫖", "Had dim sum brunch this weekend! Shrimp dumplings, siu mai, char siu bao, chicken feet... all classics. Love their custard buns - golden custard flows out with each bite, sweet but not overwhelming. Paired with pu-erh tea, the perfect way to spend a leisurely weekend.", "DimSumFanatic", "Cantonese Dim Sum"},
            {"Homemade Mexican Tacos 🌮", "Made Mexican tacos at home - simple and delicious! Corn tortilla filled with grilled beef, lettuce, tomatoes, onions, and cheese, topped with special salsa and sour cream. Each bite has rich layers of flavor - spicy, tangy, and creamy perfectly balanced. Friends loved it!", "GlobalFoodie", "Mexican Food"},
            {"Spicy Noodle Review 🍜", "This bowl of spicy noodles was absolutely amazing! Numbing and spicy with perfectly chewy noodles, topped with peanuts, pea shoots, and pickled vegetables. The key is that spoonful of chili oil and Sichuan pepper combo - so aromatic! One bowl in the morning energizes you for the whole day!", "NoodleAddict", "Noodles"},
            {"Matcha Mille Crepe Cake 🍵", "Made a matcha mille crepe cake from scratch. The process was tedious but the result was stunning! Layers of thin crepes with matcha cream, the layers are clearly visible when sliced. The bitterness of matcha perfectly balances the sweetness of cream, with a delicate silky texture. Even better after refrigerating overnight!", "MatchaObsessed", "Desserts"},
            {"Big Plate Chicken 🍗", "Made Xinjiang-style big plate chicken today! Large chunks of chicken with potatoes, bell peppers, spicy and aromatic. Finally mixed with wide noodles that absorbed all the rich sauce. The essence of this dish is in the spice combination - cumin and chili aroma is irresistible. So satisfying!", "SpicyFoodExplorer", "Chinese Regional"},
            {"Taiwanese Braised Pork Rice 🍚", "Taiwanese braised pork rice is the ultimate comfort food! Fatty and lean pork belly diced and stewed until tender and flavorful. Ladled over hot white rice with pickled cucumber and soft-boiled egg. Savory and aromatic, the rice absorbs all the braising sauce. Pure satisfaction in every bite!", "TaiwanSnackLover", "Taiwanese Cuisine"},
            {"Vietnamese Spring Rolls 🥬", "Made Vietnamese spring rolls for the first time - so fresh and light! Rice paper wrapped around shrimp, lettuce, mint leaves, and rice noodles into transparent rolls. Served with peanut sauce and fish sauce dip, the sweet, salty, and tangy flavors are complex and delicious. Low-cal and healthy, perfect for summer!", "HealthyEater", "Vietnamese Food"},
            {"Hong Kong Pineapple Bun 🍍", "Successfully made Hong Kong-style pineapple buns! The crispy sweet topping outside and soft fluffy bread inside. Add a slice of butter while hot and when you bite down, the butter melts and mixes with the bun's aroma - pure bliss! Even better than the ones from tea restaurants.", "BreadLover_Tom", "Hong Kong Style"},
            {"Indian Curry Chicken 🍛", "Tried making Indian curry chicken - the magic of spices is amazing! Turmeric, coriander seeds, cumin, cardamom... the combination of multiple spices creates unique flavors. Chicken stewed until tender, curry rich and smooth. Paired with naan or rice, absolutely delicious!", "SpiceEnthusiast", "Indian Cuisine"},
            {"Authentic Lanzhou Noodles 🍜", "Checked out an authentic Lanzhou noodle shop! Watching the chef pull the noodles is pure artistry, perfectly uniform thickness and wonderfully chewy. Clear broth with beef, radish, cilantro, and chili oil - refreshing and not greasy. One bowl warms both body and soul. This is real Lanzhou noodles!", "NoodleConnoisseur", "Chinese Noodles"},
            {"Spanish Paella 🥘", "Made Spanish paella for the first time - both presentation and taste on point! Saffron gives the rice a beautiful golden color, topped with shrimp, mussels, squid, and other seafood. The crispy bottom layer is the soul of this dish. Though preparation was a bit involved, totally worth it!", "SeafoodAddict", "Spanish Cuisine"}
        };
        
        String[] usernames = {
            "FoodExplorer", "FoodieSquad", "CulinaryLab", "MidnightDiner", "TasteJourney",
            "KitchenRookie", "DeliciousLife", "FlavorWorld", "TasteBudMemory", "ComfortFood",
            "FoodPhotographer", "HomeChef", "StreetFoodHunter", "MichelinChaser", "HomeCookKing"
        };
        
        String[] categories = {
            "Chinese", "Western", "Japanese", "Korean", "Southeast Asian", 
            "Desserts", "Beverages", "Snacks", "Baking", "Vegetarian"
        };
        
        // Food-related image URLs (using free food images from Unsplash)
        String[] imageUrls = {
            "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400",
            "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400",
            "https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?w=400",
            "https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=400",
            "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=400",
            "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400",
            "https://images.unsplash.com/photo-1551024506-0bccd828d307?w=400",
            "https://images.unsplash.com/photo-1563379926898-05f4575a45d8?w=400",
            "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=400",
            "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=400",
            "https://images.unsplash.com/photo-1559847844-5315695dadae?w=400",
            "https://images.unsplash.com/photo-1548940740-204726a19be3?w=400",
            "https://images.unsplash.com/photo-1565557623262-b51c2513a641?w=400",
            "https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=400",
            "https://images.unsplash.com/photo-1576618148400-f54bed99fcfd?w=400",
            "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400",
            "https://images.unsplash.com/photo-1551782450-a2132b4ba21d?w=400",
            "https://images.unsplash.com/photo-1581610186406-5e660e3d6646?w=400",
            "https://images.unsplash.com/photo-1481070555726-e2fe8357725c?w=400",
            "https://images.unsplash.com/photo-1516714435131-44d6b64dc6a2?w=400"
        };
        
        // Generate 20 posts
        for (int i = 0; i < foodData.length; i++) {
            Post post = new Post();
            post.setTitle(foodData[i][0]);
            post.setContent(foodData[i][1]);
            post.setUsername(foodData[i][2]);
            post.setUserId("user_" + random.nextInt(100));
            post.setCategory(foodData[i][3]);
            post.setCoverImageUrl(imageUrls[i % imageUrls.length]);
            
            // Randomly generate likes and comments count
            post.setLikesCount(random.nextInt(500) + 10);
            post.setCommentsCount(random.nextInt(100) + 1);
            
            // Randomly generate timestamp within the past 30 days
            long currentTime = System.currentTimeMillis();
            long randomTime = currentTime - (random.nextInt(30) * 24 * 60 * 60 * 1000L);
            post.setTimestamp(new Timestamp(randomTime / 1000, 0));
            
            posts.add(post);
        }
        
        return posts;
    }

    /**
     * Clear all post data (use with caution)
     */
    public void clearAllPosts(SeedCallback callback) {
        db.collection(COLLECTION_POSTS)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int totalDocs = queryDocumentSnapshots.size();
                if (totalDocs == 0) {
                    callback.onSuccess(0);
                    return;
                }
                
                int[] deletedCount = {0};
                queryDocumentSnapshots.forEach(document -> {
                    document.getReference().delete()
                        .addOnSuccessListener(aVoid -> {
                            deletedCount[0]++;
                            if (deletedCount[0] == totalDocs) {
                                callback.onSuccess(deletedCount[0]);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error deleting document: " + document.getId(), e);
                            callback.onError(e);
                        });
                });
            })
            .addOnFailureListener(callback::onError);
    }
}

