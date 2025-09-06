package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.error.ErrorCode;
import com.imovel.api.exception.ResourceNotFoundException;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.User;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.request.UserUpdateRequest;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.UserResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for handling user profile operations.
 * Manages user profile retrieval and updates with proper error handling.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    /**
     * Constructor for UserService with dependency injection.
     *
     * @param userRepository Repository for user data access operations
     * @param tokenService Service for token handling and validation
     */
    @Autowired
    public UserService(UserRepository userRepository,
                       TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    /**
     * Retrieves the current authenticated user for profile operations.
     * @param userId The ID of the user to retrieve
     * @return The authenticated user entity
     */
    private User getCurrentAuthenticatedUser(final long userId) {

        return userRepository.findById(userId).get();
    }

    /**
     * Retrieves the profile of the current authenticated user.
     *
     * @param session HTTP session containing user authentication information
     * @return StandardResponse containing the user profile DTO
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<UserResponse> getCurrentUserProfile(HttpSession session)
    {
        try{
            // Get user ID from session and retrieve the complete user entity
            User currentUser = getCurrentAuthenticatedUser(getUserId(session));

            // Log successful retrieval of user profile
            ApiLogger.info("UserService.getCurrentUserProfile", "Retrieved authenticated user"+ currentUser.getName());

            // Convert User entity to UserResponse DTO and return success response
            return ApplicationResponse.success(UserResponse.parse(currentUser).get());

        }catch (Exception e){
            // Log error details for debugging and monitoring
            ApiLogger.error(ApiCode.RESOURCE_NOT_FOUND.getMessage(), e.getMessage());

            // Return error response if user retrieval fails
            errorResponse(ApiCode.RESOURCE_NOT_FOUND,
                    "Failed to retrieve user "+ e.getMessage());
        }

        // Fallback error response if exception handling fails
        return  errorResponse(ApiCode.RESOURCE_NOT_FOUND,
                "Failed to retrieve user ");
    }

    /**
     * Extracts the user ID from the HTTP session token.
     *
     * @param session HTTP session containing the authentication token
     * @return The user ID extracted from the token
     */
    private  int getUserId(HttpSession session) {
        // Retrieve token from session attributes
        String token = (String) session.getAttribute("token");

        // Extract user ID claim from the token using token service
        String id = tokenService.getClaim("userId",token);

        // Parse the user ID string to integer and return
        return Integer.parseInt(id);
    }

    /**
     * Retrieves all users from the system.
     * Requires the requesting user to be authenticated.
     *
     * @param session HTTP session for authentication validation
     * @return List of all users in the system as UserResponse DTOs
     */
    public ApplicationResponse  <List<UserResponse>>getAllUsers(final HttpSession session) {
        try{
            // Verify the requesting user is authenticated
            final int userId = getUserId(session);
            User currentUser = getCurrentAuthenticatedUser(userId);

            // Check if current user exists (additional validation)
            if(currentUser == null){
                errorResponse(ApiCode.RESOURCE_NOT_FOUND,
                        "Failed to retrieve all users ");
            }

            // Retrieve all users from the database
            List<User> allUsers = userRepository.findAll();

            // Convert list of User entities to UserResponse DTOs and return success response
            return ApplicationResponse.success(UserResponse.parse(allUsers));

        }catch (Exception e){
            // Log error details for debugging
            ApiLogger.error(ApiCode.RESOURCE_NOT_FOUND.getMessage(), e.getMessage());

            // Return error response if user retrieval fails
            errorResponse(ApiCode.RESOURCE_NOT_FOUND,
                    "Failed to retrieve all users  "+ e.getMessage());
        }

        // Fallback error response
        return  errorResponse(ApiCode.RESOURCE_NOT_FOUND,
                "Failed to retrieve all users  ");
    }

    /**
     * Updates the profile of the current authenticated user.
     *
     * @param userRequest DTO containing the updated profile information
     * @param session HTTP session for user authentication
     * @return StandardResponse containing the updated user profile DTO
     */
    @Transactional
    public ApplicationResponse<UserResponse> updateCurrentUser(final UserUpdateRequest userRequest,
                                                               final HttpSession session) {
        try{
            // Get user ID from session and retrieve the current user entity
            final int userId = getUserId(session);
            User currentUser = getCurrentAuthenticatedUser(userId);

            // Update user fields if they are provided in the request (partial update)
            if (userRequest.getName() != null) {
                currentUser.setName(userRequest.getName());
            }
            if (userRequest.getPhone() != null) {
                currentUser.setPhone(userRequest.getPhone());
            }
            if (userRequest.getAvatar() != null) {
                currentUser.setAvatar(userRequest.getAvatar());
            }
            if (userRequest.getSocialLinks() != null) {
                // Create a new ArrayList to avoid potential shared reference issues
                currentUser.setSocialLinks(new ArrayList<>(userRequest.getSocialLinks()));
            }

            // Save the updated user entity to the database
            User updatedUser = userRepository.save(currentUser);

            // Log successful update operation
            ApiLogger.info("UserService.updateCurrentUser", "updated authenticated user"+ currentUser.getName());

            // Convert updated User entity to UserResponse DTO and return success response
            return ApplicationResponse.success(UserResponse.parse(updatedUser).get());

        }catch (Exception e){
            // Log error details
            ApiLogger.error(ApiCode.RESOURCE_NOT_FOUND.getMessage(), e.getMessage());

            // Return error response if update operation fails
            errorResponse(ApiCode.RESOURCE_NOT_FOUND,
                    "Failed to update user "+ e.getMessage());
        }

        // Fallback error response
        return  errorResponse(ApiCode.RESOURCE_NOT_FOUND,
                "Failed to update user ");
    }

    /**
     * Deletes the current authenticated user's account.
     *
     * @param session HTTP session for user authentication
     * @return StandardResponse containing the deleted user's information
     */
    @Transactional
    public ApplicationResponse<UserResponse> deleteCurrentUser(final HttpSession session) {
        try{
            // Get user ID from session and retrieve the user entity
            final int userId = getUserId(session);
            User currentUser = getCurrentAuthenticatedUser(userId);

            // Delete the user entity from the database
            userRepository.delete(currentUser);

            // Log successful deletion operation
            ApiLogger.info("UserService.deleteCurrentUser", "delete authenticated user"+ currentUser.getName());

            // Return success response with the deleted user's information
            return ApplicationResponse.success(UserResponse.parse(currentUser).get());

        }catch (Exception e){
            // Log error details
            ApiLogger.error(ApiCode.RESOURCE_NOT_FOUND.getMessage(), e.getMessage());

            // Return error response if deletion fails
            errorResponse(ApiCode.RESOURCE_NOT_FOUND,
                    "Failed to delete user "+ e.getMessage());
        }

        // Fallback error response
        return  errorResponse(ApiCode.RESOURCE_NOT_FOUND,
                "Failed to delete user ");
    }

    /**
     * Helper method to create standardized error responses.
     *
     * @param code The API error code indicating the type of error
     * @param message Detailed error message for debugging
     * @return ApplicationResponse with error details
     * @param <T> Generic type parameter for the response
     */
    private <T> ApplicationResponse<T> errorResponse(ApiCode code, String message) {
        // Log the error with service context
        ApiLogger.error("UserService.errorResponse", message, code);

        // Create and return standardized error response
        return ApplicationResponse.error(
                new ErrorCode(code.getCode(), message, code.getHttpStatus())
        );
    }
}