<?php

  $patients = $_POST['patients'];

  // echo $_FILES['profile_url']['name'][0];
  // echo $_FILES['profile_url']['name'][1];
  $file_path = "uploads/";
  foreach ($patients as $post => $field) {
    $image_name = $_FILES['profile_url']['name'][$post];
    echo $field['user_id'];
    echo "\n";
    echo $_FILES['profile_url']['name'][$post];
    echo "\n";
    $name = basename($_FILES['profile_url']['name'][$post]);
    move_uploaded_file($_FILES['profile_url']['tmp_name'][$post], $file_path . $name);
  }




?>
