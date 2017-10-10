<?php
defined('BASEPATH') OR exit('No direct script access allowed');
/**
 *
 */

class Android_upload_patients extends MY_Controller
{

  function index()
  {
    $this->load->model('users_model', 'users');
    $post = $this->input->post();
    // var_dump($_FILES);
    // var_dump($_POST);

    $post = $this->common->clean_columns(array_merge($this->users->columns, array (
            'home',
            'office',
            'mobile',
            'update_only',
            'dialog_view'
        )), $this->input->post());

    // var_dump($post);


    if (!empty($post))
    {
      $post['role_id'] = 6;
      if (empty($post['civil_status_id']))
      {
        unset($post['civil_status_id']);
      }

      $user = $this->users->create($post);
      // var_dump($user);
      // if (!isset($post['user_id']))
      // {
      //
      //
      // else
      // {
      //   $user = $this->users->update($post);
      // }

      if ($user)
      {
        $user_directory = $this->common->user_directory . $user->user_id;
        if (!file_exists($user_directory))
        {
            if (mkdir($user_directory))
            {
                chmod($user_directory, 0777);
            }
        }

        $config['upload_path'] = $user_directory;
        $config['allowed_types'] = 'jpg|jpeg|png';
        $config['overwrite'] = true;
        $config['file_name'] = $_FILES['profile_url']['name'];
        $this->load->library('upload', $config);

        $user_directory .= $this->common->directory_separator;
        if ($this->upload->do_upload('profile_url'))
        {
            // if ($user->logo && file_exists($user->profile_url))
            // {
            //     unlink($user->profile_url);
            // }
            $this->users->update(array(
                'user_id' => $user->user_id,
                'profile_url' => 'uploads/' . $user->user_id . '/' . $_FILES['profile_url']['name']
            ));
        }

        $post['home']['user_id'] = $user->user_id;
        $this->users->create_home_address($post['home']);
        $this->users->create_home_contact($post['home']);

        if (isset($post['office']))
        {
            $post['office']['user_id'] = $user->user_id;
            $this->users->create_office_address($post['office']);
            $this->users->create_office_contact($post['office']);
        }
        if (isset($post['mobile']))
        {
            $post['mobile']['user_id'] = $user->user_id;
            $this->users->create_mobile_contact($post['mobile']);
        }

        echo $user->user_id;

      }
      else
      {
        echo "failed to upload";
      }

    }
    else
    {
      echo 'post data empty';
    }
  }
}
