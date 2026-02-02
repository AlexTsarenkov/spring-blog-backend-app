

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.BackendAppApplication;
import ru.yandex.practicum.configuration.DataSourceConfiguration;
import ru.yandex.practicum.controller.PostController;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.model.Post;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendAppApplication.class)
@AutoConfigureMockMvc
public class PostControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        jdbcTemplate.execute("DELETE FROM posts");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM posts");
    }

    @Test
    void checkSinglePostsReturnIsOk() throws Exception {

        jdbcTemplate.execute("""
                        INSERT INTO posts (id, title, text, likes_count)
                        VALUES (1, 'Test Post', 'Test Post Text', 0);
                """);

        mockMvc.perform(get("/api/posts?search=&pageNumber=1&pageSize=5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andExpect(jsonPath("$.posts[0].title").value("Test Post"));
    }

    @Test
    void checkMultiplePostsReturnIsOk() throws Exception {
        jdbcTemplate.execute("""
                        INSERT INTO posts (id, title, text, likes_count)
                        VALUES (1, 'Test Post', 'Test Post Text', 0);
                """);

        jdbcTemplate.execute("""
                        INSERT INTO posts (id, title, text, likes_count)
                        VALUES (2, 'Test Post 2', 'Test Post 2 Text', 0);
                """);

        mockMvc.perform(get("/api/posts?search=&pageNumber=1&pageSize=5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts", hasSize(2)))
                .andExpect(jsonPath("$.posts[1].title").value("Test Post"))
                .andExpect(jsonPath("$.posts[0].title").value("Test Post 2"));
    }

    @Test
    void checkSearchStringGetPostsReturnIsOk() throws Exception {
        jdbcTemplate.execute("""
                        INSERT INTO posts (id, title, text, likes_count)
                        VALUES (1, 'Another one post', 'Test Post Text', 0);
                """);

        jdbcTemplate.execute("""
                        INSERT INTO posts (id, title, text, likes_count)
                        VALUES (2, 'This one good post', 'Test Post 2 Text', 0);
                """);

        mockMvc.perform(get("/api/posts?search=one&pageNumber=1&pageSize=5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts", hasSize(2)))
                .andExpect(jsonPath("$.posts[0].title").value("This one good post"))
                .andExpect(jsonPath("$.posts[1].title").value("Another one post"));

        mockMvc.perform(get("/api/posts?search=Another&pageNumber=1&pageSize=5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andExpect(jsonPath("$.posts[0].title").value("Another one post"));
    }

    @Test
    void checkPagnationGetPostsReturnIsOk() throws Exception {
        for (int i = 1; i <= 10; i++) {
            //language=SQL
            String sql = """
                            INSERT INTO posts (id, title, text, likes_count)
                            VALUES (:id, 'Another one post', 'Test Post Text', 0);
                    """;
            namedParameterJdbcTemplate.update(sql, Map.of("id", i));
        }

        mockMvc.perform(get("/api/posts?search=&pageNumber=1&pageSize=5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts", hasSize(5)));

        mockMvc.perform(get("/api/posts?search=&pageNumber=2&pageSize=5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts", hasSize(5)));

        mockMvc.perform(get("/api/posts?search=&pageNumber=1&pageSize=8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts", hasSize(8)));

        mockMvc.perform(get("/api/posts?search=&pageNumber=2&pageSize=8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts", hasSize(2)));
    }

    @Test
    void getSinglePostReturnIsOk() throws Exception {
        jdbcTemplate.execute("""
                        INSERT INTO posts (id, title, text, likes_count)
                        VALUES (1, 'Single Post', 'Test Post Text', 0);
                """);

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Single Post"))
                .andExpect(jsonPath("$.likesCount").value(0))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Test Post Text"));
    }

    @Test
    void addPostReturnIsOk() throws Exception {
        String postJsonBody = """
                        {
                            "title": "Created Post",
                            "text": "Created Post text",
                            "tags": ["tag_1", "tag_2"]
                          }
                """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postJsonBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Created Post"))
                .andExpect(jsonPath("$.text").value("Created Post text"))
                .andExpect(jsonPath("$.likesCount").value(0));

        mockMvc.perform(get("/api/posts?search=&pageNumber=1&pageSize=5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andExpect(jsonPath("$.posts[0].title").value("Created Post"));
    }

    @Test
    void updatePostReturnIsOk() throws Exception {
        jdbcTemplate.execute("""
                        INSERT INTO posts (id, title, text, likes_count)
                        VALUES (1, 'Single Post', 'Test Post Text', 2);
                """);

        String putJsonBody = """
                        {
                            "id": "1",
                            "title": "Updated Post",
                            "text": "Updated Post text",
                            "tags": ["tag_1", "tag_2"],
                            "likesCount": 2
                          }
                """;
        mockMvc.perform(put("/api/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(putJsonBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("Updated Post"))
                .andExpect(jsonPath("$.text").value("Updated Post text"))
                .andExpect(jsonPath("$.likesCount").value(2));

        mockMvc.perform(get("/api/posts?search=&pageNumber=1&pageSize=5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andExpect(jsonPath("$.posts[0].id").value("1"))
                .andExpect(jsonPath("$.posts[0].title").value("Updated Post"))
                .andExpect(jsonPath("$.posts[0].text").value("Updated Post text"))
                .andExpect(jsonPath("$.posts[0].likesCount").value(2));
    }

    @Test
    void deletePostReturnIsOk() throws Exception {
        jdbcTemplate.execute("""
                        INSERT INTO posts (id, title, text, likes_count)
                        VALUES (1, 'Single Post', 'Test Post Text', 2);
                """);

        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts?search=&pageNumber=1&pageSize=5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts", hasSize(0)));
    }

    @Test
    void createTagReturnIsOk() throws Exception {
        String postJsonBody = """
                        {
                            "title": "Created Post",
                            "text": "Created Post text",
                            "tags": ["tag_1", "tag_2"]
                          }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postJsonBody))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        Post post = mapper.readValue(createResult.getResponse().getContentAsString(), Post.class);

        MvcResult getResult = mockMvc.perform(get(String.format("/api/posts/%d", post.getId())))
                .andExpect(status().isOk())
                .andReturn();
        Post createdPost = mapper.readValue(getResult.getResponse().getContentAsString(), Post.class);

        Assertions.assertEquals(createdPost.getTags().size(), 2);
        Assertions.assertTrue(createdPost.getTags().contains("tag_1"));
        Assertions.assertTrue(createdPost.getTags().contains("tag_2"));
    }

    @Test
    void updateTagReturnIsOk() throws Exception {
        String postJsonBody = """
                        {
                            "title": "Created Post",
                            "text": "Created Post text",
                            "tags": ["tag_1", "tag_2"]
                          }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postJsonBody))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        Post post = mapper.readValue(createResult.getResponse().getContentAsString(), Post.class);

        String putJsonBody = String.format("""
                        {
                            "id": %d,
                            "title": "Created Post",
                            "text": "Created Post text",
                            "tags": ["newTag1", "newTag2", "newTag3"],
                            "likesCount": 0
                          }
                """, post.getId());

        mockMvc.perform(put("/api/posts/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(putJsonBody))
                .andExpect(status().isOk());

        MvcResult updatedPostResult = mockMvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isOk())
                .andReturn();

        Post updatedPost = mapper.readValue(updatedPostResult.getResponse().getContentAsString(), Post.class);

        Assertions.assertEquals(updatedPost.getTags().size(), 3);
        Assertions.assertTrue(updatedPost.getTags().contains("newTag1"));
        Assertions.assertTrue(updatedPost.getTags().contains("newTag2"));
        Assertions.assertTrue(updatedPost.getTags().contains("newTag3"));
    }

    @Test
    void postAddCommentReturnIsOk() throws Exception {
        jdbcTemplate.execute("""
                        INSERT INTO posts (id, title, text, likes_count)
                        VALUES (1, 'Single Post', 'Test Post Text', 2);
                """);

        String commentJson = """
                {
                    "text": "Post comment",
                    "postId": 1
                  }
                """;

        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.postId").value("1"))
                .andExpect(jsonPath("$.text").value("Post comment"));
    }

    @Test
    void postGetCommentsReturnIsOk() throws Exception {
        jdbcTemplate.execute("""
                        INSERT INTO posts (id, title, text, likes_count)
                        VALUES (1, 'Single Post', 'Test Post Text', 2);
                """);

        String commentJson1 = """
                {
                    "text": "Post comment1",
                    "postId": 1
                  }
                """;

        String commentJson2 = """
                {
                    "text": "Post comment2",
                    "postId": 1
                  }
                """;


        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentJson1))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentJson2))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void updateCommentReturnIsOk() throws Exception {
        jdbcTemplate.execute("""
                        INSERT INTO posts (id, title, text, likes_count)
                        VALUES (1, 'Single Post', 'Test Post Text', 2);
                """);

        String commentJson1 = """
                {
                    "text": "Post comment1",
                    "postId": 1
                  }
                """;

        MvcResult newComment = mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentJson1))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();

        Comment comment = mapper.readValue(newComment.getResponse().getContentAsString(), Comment.class);

        String updatedCommentJson = String.format("""
                {
                    "id": %d,
                    "text": "Completely updated Post comment",
                    "postId": 1
                  }
                """, comment.getId());

        mockMvc.perform(put("/api/posts/1/comments/" + comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedCommentJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value("Completely updated Post comment"));

        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].text").value("Completely updated Post comment"));
    }

    @Test
    void getCommentReturnIsOk() throws Exception {
        jdbcTemplate.execute("""
                        INSERT INTO posts (id, title, text, likes_count)
                        VALUES (1, 'Single Post', 'Test Post Text', 2);
                """);

        String commentJson1 = """
                {
                    "text": "Post comment1",
                    "postId": 1
                  }
                """;

        MvcResult newComment = mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentJson1))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();

        Comment comment = mapper.readValue(newComment.getResponse().getContentAsString(), Comment.class);

        mockMvc.perform(get("/api/posts/1/comments/" + comment.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value("Post comment1"));
    }

    @Test
    void deleteCommentReturnIsOk() throws Exception {
        jdbcTemplate.execute("""
                        INSERT INTO posts (id, title, text, likes_count)
                        VALUES (1, 'Single Post', 'Test Post Text', 2);
                """);

        String commentJson1 = """
                {
                    "text": "Post comment1",
                    "postId": 1
                  }
                """;

        MvcResult newComment = mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentJson1))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();

        Comment comment = mapper.readValue(newComment.getResponse().getContentAsString(), Comment.class);

        mockMvc.perform(delete("/api/posts/1/comments/" + comment.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void postLikeReturnIsOk() throws Exception {
        jdbcTemplate.execute("""
                        INSERT INTO posts (id, title, text, likes_count)
                        VALUES (1, 'Single Post', 'Test Post Text', 2);
                """);

        mockMvc.perform(post("/api/posts/1/likes"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/posts/1/likes"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/posts/1/likes"))
                .andExpect(status().isOk());

        MvcResult likedPostResult = mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        Post updatedPost = mapper.readValue(likedPostResult.getResponse().getContentAsString(), Post.class);

        Assertions.assertEquals(updatedPost.getLikesCount(), (short) 5);
    }
}

