<?php

/**
 *
 */
class Android_upload_case_records extends MY_Controller
{

  function index()
  {
    echo "hello world";
    $format = 'Y-m-d H:i:s';
    $this->load->model('cases_model', 'cases');
    $this->load->model('case_records_model', 'case_records');

    $post = $this->input->post('case_record');

    if(!empty($post))
    {

      if(!isset($post['case_record_id']))
      {
        $case_record = $this->case_records->create($post);
      }
      else
      {
        $case_record = $this->case_records->update($post);
      }

      if($case_record)
      {
        $this->case_records->create_history(array(
          'case_record_id' => $case_record->case_record_id,
          'record_status_id' => !isset($post['case_record_id']) ? 1 : 2,
          'updated_by' => $post['updated_by'],
          'updated_on' => gmdate($format)
        ));

        $attachment = $this->input->post('attachments');
        $attachment['uploaded_on'] = gmdate($format);
        
        $user_directory = $this->common->user_directory . $post['user_id'] . $this->common->directory_separator . 'case_records';
        if (!file_exists($user_directory))
        {
          if (mkdir($user_directory))
          {
              chmod($user_directory, 0777);
          }
        }
        $user_directory .= $this->common->directory_separator . $post['case_record_id'];

        if (!file_exists($user_directory))
        {
          if (mkdir($user_directory))
          {
            chmod($user_directory, 0777);
          }
        }
        $user_directory .= $this->common->directory_separator;
        $file_keys = array_keys($_FILES);

        $config['upload_path'] = $user_directory;
        $config['allowed_types'] = '*';
        $this->load->library('upload');
        foreach ($file_keys as $file_key)
        {
          $config['file_name'] = urlencode(sha1(microtime()). '-' . $_FILES[$file_key]['name']);
          $this->upload->initialize($config);

        }
      }


    }
  }

}
