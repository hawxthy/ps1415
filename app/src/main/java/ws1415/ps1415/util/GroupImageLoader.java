package ws1415.ps1415.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.Toast;

import ws1415.ps1415.R;
import ws1415.ps1415.controller.GroupController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

/**
 * Created by Bernd Eissing on 02.06.2015.
 */
public class GroupImageLoader {
    private static GroupImageLoader instance;

    private GroupImageLoader(){}

    public static GroupImageLoader getInstance(){
        if(instance== null){
            instance = new GroupImageLoader();
        }
        return instance;
    }

    public void setGroupImageToImageView(final Context context, String groupName, final ImageView imageView){
        if(groupName == null || groupName.isEmpty()){
            imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_group));
        }else{
            GroupController.getInstance().getUserGroupPicture(new ExtendedTaskDelegateAdapter<Void, Bitmap>() {
                @Override
                public void taskDidFinish(ExtendedTask task, Bitmap bitmap) {
                    if(bitmap != null){
                        imageView.setImageBitmap(bitmap);
                    }else{
                        imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_group));
                    }
                }

                @Override
                public void taskFailed(ExtendedTask task, String message) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG);
                }

            },groupName);
        }
    }
}
