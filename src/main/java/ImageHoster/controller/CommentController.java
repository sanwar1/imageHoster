package ImageHoster.controller;

import ImageHoster.model.Comment;
import ImageHoster.model.User;
import ImageHoster.service.CommentService;
import ImageHoster.service.ImageService;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CommentController {

  @Autowired
  private ImageService imageService;

  @Autowired
  private CommentService commentService;

  /*Method is called when the user submits a comment over an image. The POST request URL is dynamic
  in nature and consists of the imageId and title as Path Variables. The imageId is used to get the
  details of the image from the imageService, and the user comment (passed as String) is added to
  the Comment object and attached to the image and updated. The model return the same image's page*/
  @RequestMapping(value = "/image/{imageId}/{title}/comments", method = RequestMethod.POST)
  public String saveImageComments(@RequestParam("comment") String commentText,
      @PathVariable("imageId") Integer imageId, @PathVariable("title") String title, Model model,
      HttpSession session) throws IOException {

    User user = (User) session.getAttribute("loggeduser");

    Comment newComment = new Comment();
    newComment.setText(commentText);
    newComment.setCreatedDate(new Date());
    newComment.setUser(user);
    newComment.setImage(imageService.getImage(imageId));

    commentService.updateImageComments(newComment);

    //Converting regular String into a dynamic URL
    return "redirect:/images/" + imageId + "/" + URLEncoder
        .encode(title, StandardCharsets.UTF_8.toString());
  }
}
