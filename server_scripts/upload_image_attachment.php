<?php

  if (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['caseRecordId'])) {
    require_once('db_connect.php');

    $case_record_id = $_POST['caseRecordId'];
    $attachment_description = $_POST['description'];
    $attachment_name = $_POST['attachment_name'];
    $encoded_image = $_POST['encoded_image'];
    $attachment_type = $_POST['attachment_type'];
    $uploaded_on = $_POST['uploaded_on'];

    $SUCCESS_MESSAGE = 'SUCCESS';
    $FAILED_MESSAGE = 'FAILED';

    $file_path = "uploads/";
    $full_path = "/var/www/html/getbetter/" . $file_path;
    $attachment_path = $full_path . $attachment_name;

    $decoded_image = base64_decode($encoded_image);

    if(file_put_contents($attachment_path, $decoded_image) != false) {

      if($stmt = $mysqli->prepare('INSERT INTO tbl_case_record_attachments
        (case_record_id, description, case_file_url, case_record_attachment_type_id, uploaded_on)
        VALUES (?,?,?,?,?)')) {

          $stmt->bind_param('issis', $case_record_id, $attachment_description,
          $attachment_name, $attachment_type, $uploaded_on);

          $stmt->execute();

          $stmt->close();
      }
      echo json_encode(array('result'=>$SUCCESS_MESSAGE));
    } else {
      echo json_encode(array('result' => $FAILED_MESSAGE));
    }
$mysqli->close();
  }else {

    echo json_encode(array('result' => $FAILED_MESSAGE));
  }





?>
