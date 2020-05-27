package ImageHoster.service;

import ImageHoster.model.Image;
import ImageHoster.repository.ImageRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

  @Autowired
  private ImageRepository imageRepository;

  /*Call the getAllImages() method in the Repository and obtain a List of all the images in the DB*/
  public List<Image> getAllImages() {
    return imageRepository.getAllImages();
  }

  /*Calls the createImage() method in the Repository and passes an image to be persisted in the DB*/
  public void uploadImage(Image image) {
    imageRepository.uploadImage(image);
  }

  /*Calls the getImage() method in the Repository and passes the id of the image to be fetched*/
  public Image getImage(Integer imageId) {
    return imageRepository.getImage(imageId);
  }

  /*Calls the updateImage() method in the Repository and passes the Image to be updated in the DB*/
  public void updateImage(Image updatedImage) {
    imageRepository.updateImage(updatedImage);
  }

  /*Calls the deleteImage() method in the Repository and passes the id of the image to be deleted*/
  public void deleteImage(Integer imageId) {
    imageRepository.deleteImage(imageId);
  }

}
