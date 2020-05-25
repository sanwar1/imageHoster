package ImageHoster.controller;

import ImageHoster.model.Comment;
import ImageHoster.model.Image;
import ImageHoster.model.User;
import ImageHoster.service.CommentService;
import ImageHoster.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Controller
public class CommentController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private CommentService commentService;

    @RequestMapping(value = "/image/{imageId}/{title}/comments", method = RequestMethod.POST)
    public String saveImageComments(@RequestParam("comment") String commentText, @PathVariable("imageId") Integer imageId, @PathVariable("title") String title, Model model, HttpSession session) throws IOException {

        User user = (User) session.getAttribute("loggeduser");

        Comment newComment = new Comment();
        newComment.setText(commentText);
        newComment.setCreatedDate(new Date());
        newComment.setUser(user);

        Image image = imageService.getImageByTitle(imageId);
        List<Comment> commentList = image.getComments();
        commentList.add(newComment);
        image.setComments(commentList);

        commentService.updateImageComments(image);

        return "redirect:/images/" + imageId + "/" + URLEncoder.encode(title, StandardCharsets.UTF_8.toString());
    }
}
