<?php

/**
 *
 */
class Test extends MY_Controller
{

  function index()
  {
    $format = 'Y-m-d H:i:s';
    $this->load->model('cases_model', 'cases');
    $this->load->model('case_records_model', 'case_records');

    $post = $this->input->post();
    $_SESSION['user']->user_id = $post['case_record']['updated_by'];

    // echo $post['case_record']['control_number'];
    // var_dump($_FILES);

    $post['case_record'] = $this->common->clean_columns($this->case_records->columns, $post['case_record']);

    print_r($post);

    if(!empty($post))
    {
      $post['case_record']['case_id'] = 6;
      $case_record = $this->case_records->create($post['case_record']);

      if($case_record)
      {
        $this->case_records->create_history(array(
          'case_record_id' => $case_record->case_record_id,
          'record_status_id' => 1,
          'updated_by' => $_SESSION['user']->user_id,
          'updated_on' => gmdate($format)
        ));

        $user_directory = $this->common->user_directory . $case_record->user_id . $this->common->directory_separator . 'case_records';
        if (!file_exists($user_directory))
        {
          if (mkdir($user_directory))
          {
              chmod($user_directory, 0777);
          }
        }
        $user_directory .= $this->common->directory_separator . $case_record->case_record_id;

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
        $key = 0;

        foreach ($file_keys as $file_key)
        {
          $config['file_name'] = urlencode(sha1(microtime()) . '-' . $_FILES[$file_key]['name']);
          $this->upload->initialize($config);

          if($this->upload->do_upload($file_key))
          {
            $post['attachments'][$key]['case_file_url'] = $post['case_record']['user_id'] . '/case_records/' . $post['attachments']['case_record_id'] . '/' . $config['file_name'];
            $post['attachments'][$key]['uploaded_on'] = gmdate($format);
            $post['attachments'][$key]['case_record_id'] = $case_record->case_record_id;
            print_r($post['attachments'][$key]);
            $this->case_records->create_attachment($post['attachments'][$key]);
          }
          $key++;
        }


      }
    }
    else
    {
      echo "post data is empty";
    }
  }
}
