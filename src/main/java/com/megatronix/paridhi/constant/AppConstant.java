package com.megatronix.paridhi.constant;

/**
 * Application-wide constants used across the system.
 * Organized by functional categories for better readability and maintenance.
 */
public class AppConstant {
	private AppConstant() {
		// Private constructor to prevent instantiation
	}

	/**
	 * Service identifiers for logging
	 */
	public static final String MRD_SERVICE = "MRDService";
	public static final String OTP_SERVICE = "OtpService";
	public static final String CRD_SERVICE = "CRDService";
	public static final String USER_SERVICE = "UserService";
	public static final String TEAM_SERVICE = "TeamService";
	public static final String EMAIL_SERVICE = "EmailService";
	public static final String EVENT_SERVICE = "EventService";
	public static final String COMBO_SERVICE = "ComboService";
	public static final String GALLERY_SERVICE = "GalleryService";
	public static final String PROFILE_SERVICE = "ProfileService";
	public static final String CLOUDINARY_SERVICE = "CloudinaryService";
	public static final String USER_DETAILS_SERVICE = "UserDetailsService";
	public static final String CONTACT_QUERY_SERVICE = "ContactQueryService";
	public static final String DOMAIN_POSTER_SERVICE = "DomainPosterService";
	public static final String PASSWORD_RESET_SERVICE = "PasswordResetService";
	public static final String MEGATRONIX_TEAM_SERVICE = "MegatronixTeamService";

	/**
	 * Designation categories for team members
	 */
	public static final String MEMBER = "Member";
	public static final String MEGATRON = "Megatron";
	public static final String BARA_BHATARI = "Bara Bhatari";
	public static final String APP_DEVELOPER = "App Developer";
	public static final String BACKEND_DEVELOPER = "Backend Developer";
	public static final String FRONTEND_DEVELOPER = "Frontend Developer";
	public static final String FULL_STACK_DEVELOPER = "Full Stack Developer";
	public static final String BACKEND_DEVELOPER_AND_APP_DEVELOPER = "Backend Dev & App Dev";

	/**
	 * Team categories for club members
	 */
	public static final String MEGATRONS = "megatrons";
	public static final String DEVELOPERS = "developers";

	/**
	 * Resource types for domain entities
	 */
	public static final String MRD = "MRD";
	public static final String USER = "User";
	public static final String TEAM = "Team";
	public static final String EVENT = "Event";
	public static final String COMBO = "Combo";
	public static final String OTP = "OtpToken";
	public static final String PROFILE = "Profile";
	public static final String CONTACT_QUERY = "ContactQuery";
	public static final String MEGATRONIX_TEAM = "MegatronixTeam";
	public static final String PASSWORD_RESET_TOKEN = "PasswordResetToken";
	
	/**
	 * Resource types for media and images
	 */
	public static final String GALLERY_IMAGE = "GalleryImage";
	public static final String DOMAIN_POSTER = "DomainPoster";
	public static final String EVENT_IMAGE = "EventImage";
	public static final String TEAM_PHOTO = "TeamPhoto";
	public static final String COMBO_IMAGE = "ComboImage";
	public static final String CLOUDINARY_IMAGE = "CloudinaryImage";
	public static final String EVENT_COMBO = "EventCombo";
	public static final String GALLERY = "Gallery";

	/**
	 * Cloudinary related constants
	 */
	public static final String PUBLIC_ID = "public_id";
	public static final String SECURE_URL = "secure_url";
	public static final String CLOUDINARY_RESOURCE_TYPE = "resource_type"; 
	public static final String IMAGE = "image";
	public static final String FOLDER_KEY = "folder";
	public static final String OVERWRITE_KEY = "overwrite";
	public static final String INVALIDATE_KEY = "invalidate";
	public static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024; // 10 MB
	
	/**
	 * Request and logging attribute names
	 */
	public static final String REQUEST_ID = "requestId";
	public static final String CLIENT_IP = "clientIp";
	public static final String USER_AGENT = "userAgent";
	public static final String PATH = "path";
	public static final String METHOD = "method";
	public static final String USER_ID = "userId";
	public static final String USER_EMAIL = "userEmail";
	public static final String OPERATION = "operation";
	public static final String RESOURCE_TYPE = "resourceType";
	public static final String DETAILS = "details";
	public static final String ERROR = "error";
	public static final String CONTEXT = "context";
	
	/**
	 * Special values and placeholders
	 */
	public static final String ANONYMOUS = "anonymous";
	public static final String UNKNOWN = "unknown";
	public static final String SUCCESSFULLY_RETRIEVED = "Successfully retrieved ";
	public static final String DEFAULT_EVENT_IMAGE_SECURE_URL = "https://res.cloudinary.com/drxvzwtfr/image/upload/v1745600056/Paridhi-WHITE-FINAL_jre7gy.webp";
	public static final String DEFAULT_EVENT_IMAGE_PUBLIC_ID = "Paridhi-WHITE-FINAL_jre7gy";
	public static final String DEFAULT_PROFILE_PIC = "https://cdn-icons-png.flaticon.com/512/5951/5951752.png";
	public static final String FOR_DOMAIN = " for domain: ";
	
	/**
	 * Status indicators and codes
	 */
	public static final String FAILED = "FAILED";
	public static final String SUCCESS = "SUCCESS";
	public static final String OPEN = "OPEN";
	public static final String CLOSED = "CLOSED";
	public static final String RESOLVED = "RESOLVED";
	public static final String UNRESOLVED = "UNRESOLVED";
	
	/**
	 * Security related constants
	 */
	public static final String ACCESS_DENIED = "access_denied";
	public static final String ACCESS_GRANTED = "access_granted";
	public static final String AUTHENTICATION_SUCCESS = "authentication_success";
	public static final String AUTHENTICATION_FAILURE = "authentication_failure";
	public static final String LOGOUT_SUCCESS = "logout_success";
	public static final String TOKEN_EXPIRED = "token_expired";
	public static final String TOKEN_INVALID = "token_invalid";
	
	/**
	 * UI/Email style constants
	 */
	public static final String PRIMARY_COLOR = "#1562D4";        // Blue
	public static final String SECONDARY_COLOR = "#64C882";      // Green
	public static final String BACKGROUND_COLOR = "#222222";     // Dark gray
	public static final String TEXT_COLOR = "#CCCCCC";           // Light gray
	public static final String H3_TEXT_COLOR = "#66c2ff";				 // Light blue
	public static final String BORDER_BOTTOM_COLOR = "#0040ff";  // Blue
	public static final String H_COLOR = "#FFFFFF";           	 // White
	public static final String HEADER_BACKGROUND = "#1A1A1A";    // Even darker gray
	public static final String FOOTER_COLOR = "#777777";         // Medium gray
	public static final String FIRST_PLACE_COLOR = "#FFD700";    // Gold
	public static final String SECOND_PLACE_COLOR = "#C0C0C0";   // Silver
	public static final String THIRD_PLACE_COLOR = "#CD7F32";    // Bronze
}