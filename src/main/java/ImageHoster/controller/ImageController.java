package ImageHoster.controller;

import ImageHoster.model.Image;
import ImageHoster.model.Tag;
import ImageHoster.model.User;
import ImageHoster.service.ImageService;
import ImageHoster.service.TagService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ImageController {

  @Autowired
  private ImageService imageService;

  @Autowired
  private TagService tagService;

  //This method displays all the images in the user home page after successful login
  @RequestMapping("images")
  public String getUserImages(Model model) {
    List<Image> images = imageService.getAllImages();
    model.addAttribute("images", images);
    return "images";
  }

  /*This method is called when details of the specific image and the title are to be displayed
  The logic is to get the image from the database with the imageId (As titles are non-unique)
  Receive the dynamic parameter from the URL in a string variable 'title' and also a Model object
  Call the getImage() method in the business logic to fetch all the details of that image
  Add the image in the Model type object with 'image' as the key
  Return 'images/image.html' file. Also now you need to add the tags and comments of an image in the
  Model type object*/
  @RequestMapping("/images/{imageId}/{title}")
  public String showImage(@PathVariable("imageId") Integer imageId,
      @PathVariable("title") String title, Model model) throws NullPointerException {
    Image image = imageService.getImage(imageId);
    List<Tag> tags = image.getTags();
    model.addAttribute("image", image);
    model.addAttribute("tags", tags);
    model.addAttribute("comments", image.getComments());
    return "images/image";
  }

  //This controller method is called when the request pattern is of type 'images/upload'
  //The method returns 'images/upload.html' file
  @RequestMapping("/images/upload")
  public String newImage() {
    return "images/upload";
  }

  /*This  method is called when 'images/upload' is requested and also the request is of POST type
  The method receives all the details of the image to be stored (persisted) in the database
  by sending it to the business logic. After you get the imageFile, set the user of the image
  by getting the logged in user from the Http Session. Convert the image to Base64 format
  and store it as a string in the 'imageFile' attribute. Set the date on which the image is
  posted. After storing the image, this method directs to the logged in user homepage
  displaying all the images.
  Get the 'tags' request parameter (String of all tags) using @RequestParam annotation
  Store all the tags in the database and make a List using the findOrCreateTags() method
  set the tags attribute of the image as a List returned by the findOrCreateTags() method
  If the tags String is empty, assign a default tag called 'Image'*/
  @RequestMapping(value = "/images/upload", method = RequestMethod.POST)
  public String createImage(@RequestParam("file") MultipartFile file,
      @RequestParam("tags") String tags, Image passedImage, HttpSession session)
      throws IOException {

    Image newImage = new Image();
    newImage.setTitle(passedImage.getTitle());
    newImage.setDescription(passedImage.getDescription());

    User user = (User) session.getAttribute("loggeduser");
    newImage.setUser(user);

    String uploadedImageData = convertUploadedFileToBase64(file);
    newImage.setImageFile(uploadedImageData);

    if (!tags.isEmpty()) {
      List<Tag> imageTags = findOrCreateTags(tags);
      newImage.setTags(imageTags);
    } else {
      List<Tag> imageTags = findOrCreateTags("Image");
      newImage.setTags(imageTags);
    }

    newImage.setDate(new Date());
    imageService.uploadImage(newImage);
    return "redirect:/images";
  }

  /*This controller method is called when the request pattern is of type 'editImage'. This method
  fetches the image by imageId from the database and adds it to the model with key as 'image'
  The method then returns 'images/edit.html' file wherein user updates the details of the image
  The method first needs to convert the list of all the tags to a string containing all the tags
  separated by a comma and then add this string in a Model type object
  This string is then displayed by 'edit.html' file as previous tags of an image
  Before the page is passed, the ownership of the image is checked against the logged user
  If the image Owner is same, the edit page is displayed, or an error message is passed.*/
  @RequestMapping(value = "/editImage")
  public String editImage(@RequestParam("imageId") Integer imageId, Model model,
      HttpSession session) {
    Image image = imageService.getImage(imageId);

    User sessionUser = (User) session.getAttribute("loggeduser");
    User imageUser = image.getUser();

    if (sessionUser.getUsername().equals(imageUser.getUsername())) {
      model.addAttribute("image", image);
      List<Tag> tagsList = image.getTags();
      String tags = convertTagsToString(image.getTags());
      model.addAttribute("tags", tags);
      //model.addAttribute("comments", image.getComments());
      return "images/edit";
    } else {
      List<Tag> tags = image.getTags();
      model.addAttribute("image", image);
      model.addAttribute("tags", tags);
      model.addAttribute("comments", image.getComments());
      String error = "Only the owner of the image can edit the image";
      model.addAttribute("editError", error);
      return "images/image";
    }
  }

  /*This controller method is called when the request pattern is of type 'images/edit' and also
   the incoming request is of PUT type. The method receives the imageFile, imageId, updated image,
   along with the Http Session. The method adds the new imageFile to the updated image if user
   updates the imageFile and adds the previous imageFile to the new updated image if user does not
   choose to update the imageFile. Set an id of the new updated image. Set the user using
   Http Session. Set the date on which the image is posted. Call the updateImage() method in the
   business logic to update the image. Direct to the same page showing the details of that
   particular updated image.*/

  /*The method also receives tags parameter which is a string of all the tags separated by a comma
  using the annotation @RequestParam. The method converts the string to a list of all the tags
  using findOrCreateTags() method and sets the tags attribute of an image as list of all the tags*/
  @RequestMapping(value = "/editImage", method = RequestMethod.PUT)
  public String editImageSubmit(@RequestParam("file") MultipartFile file,
      @RequestParam("imageId") Integer imageId, @RequestParam("tags") String tags,
      Image updatedImage, HttpSession session) throws IOException {

    Image image = imageService.getImage(imageId);
    String updatedImageData = convertUploadedFileToBase64(file);

    List<Tag> imageTags = findOrCreateTags(tags);

    if (updatedImageData.isEmpty()) {
      updatedImage.setImageFile(image.getImageFile());
    } else {
      updatedImage.setImageFile(updatedImageData);
    }

    updatedImage.setId(imageId);
    User user = (User) session.getAttribute("loggeduser");
    updatedImage.setUser(user);
    updatedImage.setTags(imageTags);
    updatedImage.setDate(new Date());
    updatedImage.setComments(image.getComments());

    imageService.updateImage(updatedImage);
    return "redirect:/images/" + updatedImage.getId() + "/" + updatedImage.getTitle();
  }


  /*This controller method is called when the request pattern is of type 'deleteImage' and also the
  incoming request is of DELETE type. The method calls the deleteImage() method in the
  business logic passing the id of the image to be deleted. Looks for a controller method with
  request mapping of type '/images'*/
  @RequestMapping(value = "/deleteImage", method = RequestMethod.DELETE)
  public String deleteImageSubmit(@RequestParam(name = "imageId") Integer imageId, Model model,
      HttpSession session) {

    Image image = imageService.getImage(imageId);

    User sessionUser = (User) session.getAttribute("loggeduser");
    User imageUser = image.getUser();

    if (sessionUser.getUsername().equals(imageUser.getUsername())) {
      imageService.deleteImage(imageId);
      return "redirect:/images";
    } else {
      String error = "Only the owner of the image can delete the image";
      model.addAttribute("deleteError", error);
      model.addAttribute("image", image);
      model.addAttribute("tags", image.getTags());
      return "images/image";
    }
  }

  /*This method converts the image to Base64 format*/
  private String convertUploadedFileToBase64(MultipartFile file) throws IOException {
    return Base64.getEncoder().encodeToString(file.getBytes());
  }

  /*findOrCreateTags() method has been implemented, which returns the list of tags after converting
  the ‘tags’ string to a list of all tags and also stores the tags in the DB if they do not exist in
  the database. Observe the method and complete the code where required for this method. Try to get
  the tag from the database using getTagByName() method. If tag is returned, you need not to store
  that tag in the database, and if null is returned, you need to first store that tag in the DB and
  then the tag is added to a list. After adding all tags to a list, the list is returned*/
  private List<Tag> findOrCreateTags(String tagNames) {
    StringTokenizer st = new StringTokenizer(tagNames, ",");
    List<Tag> tags = new ArrayList<Tag>();

    while (st.hasMoreTokens()) {
      String tagName = st.nextToken().trim();
      Tag tag = tagService.getTagByName(tagName);

      if (tag == null) {
        Tag newTag = new Tag(tagName);
        tag = tagService.createTag(newTag);
      }
      tags.add(tag);
    }
    return tags;
  }

  /*The method receives the list of all tags. Converts the list of all tags to a single string
  containing all the tags separated by a comma. Returns the string*/
  private String convertTagsToString(List<Tag> tags) {
    StringBuilder tagString = new StringBuilder();

    for (int i = 0; i <= tags.size() - 2; i++) {
      tagString.append(tags.get(i).getName()).append(",");
    }

    Tag lastTag = tags.get(tags.size() - 1);
    tagString.append(lastTag.getName());

    return tagString.toString();
  }
}
