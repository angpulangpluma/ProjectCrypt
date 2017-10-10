<?php

  header('Content-type : bitmap; charset=utf-8');

  if (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['id'])) {
    require_once('db_connect.php');

    $patient_id = $_POST['id'];
    $patient_first_name = $_POST['firstName'];
    $patient_middle_name = $_POST['middleName'];
    $patient_last_name = $_POST['lastName'];

    $birthdate = $_POST['birthdate'];
    $gender = $_POST['genderId'];
    $civil_status = $_POST['civilStatusId'];
    $health_center = $_POST['healthCenterId'];

    $role_id = 6;
    $gender_id = 1;
    $civil_status_id = 1;

    $file_path = "uploads/";
    $full_path = "http://128.199.205.226/get_better/" . $file_path;
    $image_name = $_POST['imageName'];
    $profile_image_path = $full_path . $image_name;
    $image = $_POST['image'];
    $decoded_image = base64_decode($image);

    $SUCCESS_MESSAGE = 'SUCCESS';
    $FAILED_MESSAGE = 'FAILED';

    if($gender_stmt = $mysqli->prepare("SELECT gender_id FROM tbl_genders WHERE gender_name = ?")) {

      $gender_stmt->bind_param('s', $gender);

      $gender_stmt->execute();

      $gender_stmt->bind_result($gender_id);

      $gender_stmt->fetch();

      $gender_stmt->close();
    }

    if($cs_stmt = $mysqli->prepare("SELECT civil_status_id FROM tbl_civil_statuses WHERE civil_status_name = ?")) {

      $cs_stmt->bind_param('s', $civil_status);

      $cs_stmt->execute();

      $cs_stmt->bind_result($civil_status_id);

      $cs_stmt->fetch();

      $cs_stmt->close();
    }
    if(file_put_contents($file_path . $image_name, $decoded_image) != false) {

      if($stmt = $mysqli->prepare("INSERT INTO tbl_users (user_id, first_name, middle_name, last_name,
      birthdate, gender_id, civil_status_id, role_id, profile_url, default_health_center)
      VALUES (?,?,?,?,?,?,?,?,?,?)")) {

        $stmt->bind_param('issssiiisi', $patient_id, $patient_first_name,
        $patient_middle_name, $patient_last_name, $birthdate, $gender_id,
        $civil_status_id, $role_id, $profile_image_path, $health_center);

        $stmt->execute();

        $stmt->close();

        // array_push($result, array('success' => 'SUCCESS'));
        // $result = array('message'=>$SUCCESS_MESSAGE);
        //
        // echo json_encode(array('result' => $result));
        echo json_encode(array('result'=>$SUCCESS_MESSAGE));
      } else {


        echo json_encode(array('result' => $FAILED_MESSAGE));

      }

    }

  $mysqli->close();

  } else {

    echo json_encode(array('result' => $FAILED_MESSAGE));

  }

?>
