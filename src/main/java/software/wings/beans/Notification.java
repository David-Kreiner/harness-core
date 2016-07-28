package software.wings.beans;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import org.mongodb.morphia.annotations.Entity;

import javax.validation.constraints.NotNull;

/**
 * Created by anubhaw on 7/22/16.
 */
@Entity(value = "notifications")
@JsonTypeInfo(use = Id.NAME, property = "notificationType")
@JsonSubTypes({
  @Type(ApprovalNotification.class)
  , @Type(FailureNotification.class), @Type(ChangeNotification.class), @Type(InformationNotification.class)
})
public abstract class Notification extends Base {
  private String environmentId;
  private String entityId;
  private NotificationEntityType entityType;
  @NotNull private NotificationType notificationType;

  /**
   * Instantiates a new Notification.
   */
  public Notification() {}

  /**
   * Instantiates a new Notification.
   *
   * @param notificationType the notification type
   */
  public Notification(NotificationType notificationType) {
    this.notificationType = notificationType;
  }

  public String getEnvironmentId() {
    return environmentId;
  }

  public void setEnvironmentId(String environmentId) {
    this.environmentId = environmentId;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public NotificationEntityType getEntityType() {
    return entityType;
  }

  public void setEntityType(NotificationEntityType entityType) {
    this.entityType = entityType;
  }

  public NotificationType getNotificationType() {
    return notificationType;
  }

  public void setNotificationType(NotificationType notificationType) {
    this.notificationType = notificationType;
  }

  /**
   * The enum Notification type.
   */
  public enum NotificationType {
    /**
     * Approval notification type.
     */
    APPROVAL, /**
               * Change notification type.
               */
    CHANGE, /**
             * Failure notification type.
             */
    FAILURE,

    INFORMATION
  }

  public enum NotificationEntityType { ARTIFACT, RELEASE, WORKFLOW, DEPLOYMENT }

  public enum NotificationAction { ACCEPT, REJECT, RESUME }
}
