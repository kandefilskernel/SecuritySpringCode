package StudentsSecurity.service;

import StudentsSecurity.dto.ReqRes;
import StudentsSecurity.entity.OurUsers;
import StudentsSecurity.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class UsersManagementService {
  @Autowired
  private UserRepo userRepo;

  @Autowired
  private  JWTUtils jwtUtils;

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public ReqRes register(ReqRes registrationRequest){
      ReqRes res=new ReqRes();
      try {
        OurUsers ourUser=new OurUsers();
        ourUser.setEmail(registrationRequest.getEmail());
        ourUser.setCity(registrationRequest.getCity());
        ourUser.setRole(registrationRequest.getRole());
        ourUser.setName(registrationRequest.getName());
        ourUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        OurUsers ourUsersResult = userRepo.save(ourUser);
        if(ourUsersResult.getId()>0)
        {
          res.setOurUsers(ourUsersResult);
          res.setMessage("User Saved successfully");
          res.setStatusCode(200);
        }
      }catch (Exception e){
         res.setStatusCode(500);
         res.setError(e.getMessage());
      }
      return res;
  }
  public ReqRes login(ReqRes loginRequest){
       ReqRes response=new ReqRes();
       try {
             authenticationManager.authenticate(
               new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword()));
               var user = userRepo.findByEmail(loginRequest.getEmail()).orElseThrow();
               var jwt = jwtUtils.generateToken(user);
               var refreshToken =  jwtUtils.generateRefreshToken(new HashMap<>(), user);
               response.setStatusCode(200);
               response.setToken(jwt);
               response.setRefreshToken(refreshToken);
               response.setExpirationTime("24Hrs");
               response.setMessage("successfully logged In");
       }
       catch (Exception e)
       {
          response.setStatusCode(500);
          response.setMessage(e.getMessage());
       }
       return response;
  }
  public ReqRes refreshToken(ReqRes refreshTokenRegister){
    ReqRes response=new ReqRes();
    try {
         String ourEmail = jwtUtils.extractUsername(refreshTokenRegister.getToken());
         OurUsers users = userRepo.findByEmail(ourEmail).orElseThrow();
         if(jwtUtils.isTokenValid(refreshTokenRegister.getToken(),users)){
            var jwt = jwtUtils.generateToken(users);
           response.setStatusCode(200);
           response.setToken(jwt);
           response.setRefreshToken(refreshTokenRegister.getRefreshToken());
           response.setExpirationTime("24Hrs");
           response.setMessage("successfully Refresh Token");
         }
         response.setStatusCode(200);
         return  response;
    }catch (Exception e){
       response.setStatusCode(500);
       response.setMessage(e.getMessage());
       return response;
    }
  }
  public ReqRes getAllUsers(){
       ReqRes reqRes = new ReqRes();
       try {
         List<OurUsers> result = userRepo.findAll();
         if(!result.isEmpty()){
              reqRes.setOurUsersList(result);
              reqRes.setStatusCode(200);
              reqRes.setMessage("successfully");
         }else {
           reqRes.setStatusCode(404);
           reqRes.setMessage("No found user");
         }
         return  reqRes;
       }catch (Exception e){
         reqRes.setStatusCode(404);
         reqRes.setMessage("Error occurred"+ e.getMessage());
         return reqRes;
       }
  }

  public  ReqRes getUsersById(Integer id){
     ReqRes reqRes = new ReqRes();
     try {
              OurUsers usersById= userRepo.findById(id).orElseThrow(()-> new RuntimeException("User not found"));
              reqRes.setOurUsers(usersById);
              reqRes.setStatusCode(200);
              reqRes.setMessage("Users With id '" + id + "' found successfully");
     }catch (Exception kernel){
       reqRes.setStatusCode(500);
       reqRes.setMessage("Error occurred"+ kernel.getMessage());
     }
     return reqRes;
  }

  public  ReqRes deleteUser(Integer userId){
    ReqRes reqRes = new ReqRes();
    try {
      Optional<OurUsers> userOptional = userRepo.findById(userId);
      if (userOptional.isPresent()){
         userRepo.deleteById(userId);
        reqRes.setStatusCode(200);
        reqRes.setMessage("User deleted successfully");
      }
    }catch (Exception kernel){
      reqRes.setStatusCode(500);
      reqRes.setMessage("Error occurred while deleting user:"+ kernel.getMessage());
    }
    return  reqRes;
  }

  public ReqRes updateUser(Integer userId, OurUsers updateUser){
    ReqRes reqRes = new ReqRes();
    try {
      Optional<OurUsers> userOptional = userRepo.findById(userId);
      if (userOptional.isPresent()){
          OurUsers existingUser = userOptional.get();
          existingUser.setEmail(updateUser.getEmail());
          existingUser.setName(updateUser.getName());
          existingUser.setCity(updateUser.getCity());
          existingUser.setRole(updateUser.getRole());

          if(updateUser.getPassword()!=null && !updateUser.getPassword().isEmpty())
          {
            existingUser.setPassword(passwordEncoder.encode(updateUser.getPassword()));
          }

          OurUsers savedUser= userRepo.save(existingUser);
          reqRes.setOurUsers(savedUser);
          reqRes.setStatusCode(200);
          reqRes.setMessage("User updated successfully");
      }
      else
      {
        reqRes.setStatusCode(404);
        reqRes.setMessage("User not found for update");
      }
    }catch (Exception kernel){
      reqRes.setStatusCode(500);
      reqRes.setMessage("Error occurred while updating user:"+ kernel.getMessage());
    }
    return  reqRes;
  }

  public ReqRes getMyInfo(String email){
    ReqRes reqRes = new ReqRes();
    try {
      Optional<OurUsers> userOptional = userRepo.findByEmail(email);
      if (userOptional.isPresent()){
        reqRes.setOurUsers(userOptional.get());
        reqRes.setStatusCode(200);
        reqRes.setMessage("successful");
      }
      else
      {
        reqRes.setStatusCode(404);
        reqRes.setMessage("User not found for update");
      }
    }catch (Exception kernel){
      reqRes.setStatusCode(500);
      reqRes.setMessage("Error occurred while getting user info:"+ kernel.getMessage());
    }
    return  reqRes;
  }
}
