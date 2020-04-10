export class NotificationUtil {
  public static notify(message: string) {
    if (!('Notification' in window)) {
      // Browser dont support notifications
      return;
    }

    if (Notification.permission === 'granted') {
      new Notification(message);
    } else if (Notification.permission !== 'denied') {
      Notification.requestPermission(function(permission) {
        if (permission === 'granted') {
          new Notification(message);
        }
      });
    }
  }

  private constructor() {}
}
